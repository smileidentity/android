package com.smileidentity.viewmodel

import android.graphics.Bitmap
import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Rect
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.models.v2.FailureReason
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.models.v2.SelfieImageOriginValue.BackCamera
import com.smileidentity.models.v2.SelfieImageOriginValue.FrontCamera
import com.smileidentity.models.v2.SmartSelfieResponse
import com.smileidentity.models.v2.asNetworkRequest
import com.smileidentity.networking.doSmartSelfieAuthentication
import com.smileidentity.networking.doSmartSelfieEnrollment
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.area
import com.smileidentity.util.calculateLuminance
import com.smileidentity.util.createLivenessFile
import com.smileidentity.util.createSelfieFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.postProcessImageBitmap
import com.smileidentity.util.rotated
import com.smileidentity.viewmodel.SelfieHint.EnsureDeviceUpright
import com.smileidentity.viewmodel.SelfieHint.EnsureEntireFaceVisible
import com.smileidentity.viewmodel.SelfieHint.LookStraight
import com.smileidentity.viewmodel.SelfieHint.MoveBack
import com.smileidentity.viewmodel.SelfieHint.MoveCloser
import com.smileidentity.viewmodel.SelfieHint.NeedLight
import com.smileidentity.viewmodel.SelfieHint.OnlyOneFace
import com.smileidentity.viewmodel.SelfieHint.PoorImageQuality
import com.smileidentity.viewmodel.SelfieHint.SearchingForFace
import com.smileidentity.viewmodel.SelfieHint.Smile
import com.ujizin.camposer.state.CamSelector
import java.io.File
import java.io.IOException
import kotlin.math.absoluteValue
import kotlin.time.TimeSource
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import retrofit2.HttpException
import timber.log.Timber

/*
This is used only when NOT in strict mode. In strict mode, the number of images is determined
by the liveness task
 */
const val VIEWFINDER_SCALE = 1.3f
private const val COMPLETED_DELAY_MS = 1500L
private const val FACE_QUALITY_THRESHOLD = 0.5f
private const val FORCED_FAILURE_TIMEOUT_MS = 20_000L
private const val IGNORE_FACES_SMALLER_THAN = 0.03f
private const val INTRA_IMAGE_MIN_DELAY_MS = 250
private const val LIVENESS_IMAGE_SIZE = 320
private const val LOADING_INDICATOR_DELAY_MS = 100L
private const val LUMINANCE_THRESHOLD = 50
private const val MAX_FACE_FILL_THRESHOLD = 0.3f
private const val MAX_FACE_PITCH_THRESHOLD = 30
private const val MAX_FACE_ROLL_THRESHOLD = 30
private const val MAX_FACE_YAW_THRESHOLD = 15
private const val MIN_FACE_FILL_THRESHOLD = 0.1f
private const val NO_FACE_RESET_DELAY_MS = 500
private const val NUM_LIVENESS_IMAGES = 8
private const val SELFIE_IMAGE_SIZE = 640
private const val SELFIE_QUALITY_HISTORY_LENGTH = 5

sealed interface SelfieState {
    data class Analyzing(val hint: SelfieHint) : SelfieState
    data object Processing : SelfieState
    data class Error(val throwable: Throwable) : SelfieState
    data class Success(val result: SmartSelfieResponse) : SelfieState
}

enum class SelfieHint(@DrawableRes val animation: Int, @StringRes val text: Int) {
    SearchingForFace(
        R.drawable.si_tf_face_search,
        R.string.si_smart_selfie_v2_directive_place_entire_head_in_frame,
    ),
    EnsureDeviceUpright(
        R.drawable.si_tf_face_search,
        R.string.si_smart_selfie_v2_directive_ensure_device_upright,
    ),
    OnlyOneFace(-1, R.string.si_smart_selfie_v2_directive_ensure_one_face),
    EnsureEntireFaceVisible(-1, R.string.si_smart_selfie_v2_directive_ensure_entire_face_visible),
    NeedLight(R.drawable.si_tf_light_flash, R.string.si_smart_selfie_v2_directive_need_more_light),
    MoveBack(-1, R.string.si_smart_selfie_v2_directive_move_back),
    MoveCloser(-1, R.string.si_smart_selfie_v2_directive_move_closer),
    PoorImageQuality(
        R.drawable.si_tf_light_flash,
        R.string.si_smart_selfie_v2_directive_poor_image_quality,
    ),
    LookLeft(-1, R.string.si_smart_selfie_v2_directive_look_left),
    LookRight(-1, R.string.si_smart_selfie_v2_directive_look_right),
    LookUp(-1, R.string.si_smart_selfie_v2_directive_look_up),
    LookStraight(-1, R.string.si_smart_selfie_v2_directive_keep_looking),
    Smile(-1, R.string.si_smart_selfie_v2_directive_smile),
}

data class SmartSelfieV2UiState(
    val selfieState: SelfieState = SelfieState.Analyzing(SearchingForFace),
)

@kotlin.OptIn(FlowPreview::class)
class SmartSelfieV2ViewModel(
    private val userId: String,
    private val isEnroll: Boolean,
    private val useStrictMode: Boolean,
    private val selfieQualityModel: SelfieQualityModel,
    private val metadata: MutableList<Metadatum>,
    private val allowNewEnroll: Boolean? = null,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    private val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().apply {
            setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        }.build(),
    ),
    private val onResult: SmileIDCallback<SmartSelfieResult>,
) : ViewModel() {
    private val activeLiveness = ActiveLivenessTask()

    private val _uiState = MutableStateFlow(SmartSelfieV2UiState())
    val uiState = _uiState.asStateFlow().sample(500).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        _uiState.value,
    )
    private val livenessFiles = mutableListOf<File>()
    private var selfieFile: File? = null
    private var lastAutoCaptureTimeMs = 0L
    private var lastValidFaceDetectTime = 0L
    private var shouldAnalyzeImages = true
    private var selfieCameraOrientation: Int? = null
    private val modelInputSize = intArrayOf(1, 120, 120, 3)
    private val selfieQualityHistory = mutableListOf<Float>()
    private var forcedFailureTimerExpired = false
    private val shouldUseActiveLiveness: Boolean get() = useStrictMode && !forcedFailureTimerExpired
    private val metadataTimerStart = TimeSource.Monotonic.markNow()

    init {
        startStrictModeTimerIfNecessary()
    }

    /**
     * In strict mode, the user has a certain amount of time to finish the active liveness task. If
     * the user exceeds this time limit, the job will be explicitly failed by setting a flag on the
     * API request. Static liveness images will be captured for this
     */
    private fun startStrictModeTimerIfNecessary() {
        if (useStrictMode) {
            viewModelScope.launch {
                delay(FORCED_FAILURE_TIMEOUT_MS)
                val selfieState = uiState.value.selfieState
                // These 2 conditions should theoretically both be true at the same time
                if (!activeLiveness.isFinished && selfieState is SelfieState.Analyzing) {
                    SmileIDCrashReporting.hub.addBreadcrumb("Strict Mode force fail timer expired")
                    Timber.d("Strict Mode forced failure timer expired")
                    forcedFailureTimerExpired = true
                    resetCaptureProgress(LookStraight)
                }
            }
        }
    }

    /**
     * This checks all conditions of the camera frame. It returns eagerly. The checks performed are:
     * 1. Whether enough time has elapsed since the last capture
     * 2. Luminance
     * 3. Face detection
     * 4. More than one face detected
     * 5. Face landmark contours bounding box within image ("entire face visible")
     * 6. Face too far from camera
     * 7. Face too close to camera
     * 8. For selfie images only:
     *   a. Face looking straight ahead
     *   b. Face quality model
     * 9. For liveness images only in strict mode AND strict mode timer is not expired:
     *   a. Face looking in the correct direction
     */
    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy, camSelector: CamSelector) {
        val elapsedTimeSinceCaptureMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        val enoughTimeHasPassed = elapsedTimeSinceCaptureMs > INTRA_IMAGE_MIN_DELAY_MS
        if (!enoughTimeHasPassed || !shouldAnalyzeImages) {
            imageProxy.close()
            return
        }

        val image = imageProxy.image ?: run {
            Timber.w("ImageProxy has no image")
            SmileIDCrashReporting.hub.addBreadcrumb("ImageProxy has no image")
            imageProxy.close()
            return
        }

        // We want to hold the orientation constant for the duration of the capture
        val desiredOrientation = selfieCameraOrientation ?: imageProxy.imageInfo.rotationDegrees
        if (imageProxy.imageInfo.rotationDegrees != desiredOrientation) {
            val message = "Camera orientation changed. Resetting progress"
            Timber.d(message)
            SmileIDCrashReporting.hub.addBreadcrumb(message)
            resetCaptureProgress(EnsureDeviceUpright)
            imageProxy.close()
            selfieCameraOrientation = null
            return
        }

        // YUV_420_888 is the format produced by CameraX and needed for Luminance calculation
        check(imageProxy.format == YUV_420_888) { "Unsupported format: ${imageProxy.format}" }
        val luminance = calculateLuminance(imageProxy)
        if (luminance < LUMINANCE_THRESHOLD) {
            Timber.d("Low luminance detected")
            conditionFailedWithReasonAndTimeout(NeedLight)
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            val bmp = imageProxy.toBitmap().rotated(imageProxy.imageInfo.rotationDegrees)
            val viewfinderWidth = (bmp.width / VIEWFINDER_SCALE).toInt()
            val viewfinderHeight = (bmp.height / VIEWFINDER_SCALE).toInt()
            val viewfinderRect = Rect(
                (bmp.width - viewfinderWidth) / 2,
                (bmp.height - viewfinderHeight) / 2,
                bmp.width - ((bmp.width - viewfinderWidth) / 2),
                bmp.height - ((bmp.height - viewfinderHeight) / 2),
            )
            // Ignore faces outside the zoomed in viewfinder (i.e. Provide feedback only on what the
            // user sees) and ignore very small faces (i.e. if user is in a crowded location)
            // https://smileidentity.slack.com/archives/C049K02DQU9/p1717552675191829?thread_ts=1717551669.342599&cid=C049K02DQU9
            val facesToConsider = faces.filter {
                viewfinderRect.contains(it.boundingBox.centerX(), it.boundingBox.centerY()) &&
                    it.boundingBox.area / inputImage.area.toFloat() > IGNORE_FACES_SMALLER_THAN
            }

            if (facesToConsider.size > 1) {
                Timber.d("More than one face detected")
                conditionFailedWithReasonAndTimeout(OnlyOneFace)
                return@addOnSuccessListener
            }

            val face = facesToConsider.firstOrNull() ?: run {
                Timber.d("No face detected")
                conditionFailedWithReasonAndTimeout(SearchingForFace)
                return@addOnSuccessListener
            }

            // The face contour is used for the Selfie Quality Model later. Sometimes, the contours
            // extend beyond the face bounding box, so we have to check the bounds explicitly
            // Get the min and max x and y coordinates of the face mesh contour points
            val allContourPoints = face.allContours.flatMap { it.points }
            if (allContourPoints.isEmpty()) {
                Timber.d("No face contour points detected")
                // No directive update here because there is nothing the user can do
                return@addOnSuccessListener
            }
            val contourRect = Rect(
                allContourPoints.minOf { it.x }.toInt(),
                allContourPoints.minOf { it.y }.toInt(),
                allContourPoints.maxOf { it.x }.toInt(),
                allContourPoints.maxOf { it.y }.toInt(),
            )

            if (!viewfinderRect.contains(contourRect)) {
                Timber.d("Face contour not within image")
                conditionFailedWithReasonAndTimeout(EnsureEntireFaceVisible)
                return@addOnSuccessListener
            }

            // Check that the face is close enough to the camera
            val faceFillRatio = (face.boundingBox.area / inputImage.area.toFloat())
            if (faceFillRatio < MIN_FACE_FILL_THRESHOLD) {
                Timber.d("Face not close enough to camera")
                conditionFailedWithReasonAndTimeout(MoveCloser)
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            if (faceFillRatio > MAX_FACE_FILL_THRESHOLD) {
                Timber.d("Face too close to camera")
                conditionFailedWithReasonAndTimeout(MoveBack)
                return@addOnSuccessListener
            }

            // Active liveness tasks may cause temporary loss of detected face. We want to provide a
            // time buffer for such occurrences before we reset. However, after said time buffer,
            // we should reset.
            lastValidFaceDetectTime = System.currentTimeMillis()

            val selfieFile = this.selfieFile // for smart casting purposes
            if (selfieFile == null) {
                // Reject extreme head poses
                val extremePitch = face.headEulerAngleX.absoluteValue > MAX_FACE_PITCH_THRESHOLD
                val extremeYaw = face.headEulerAngleY.absoluteValue > MAX_FACE_YAW_THRESHOLD
                val extremeRoll = face.headEulerAngleZ.absoluteValue > MAX_FACE_ROLL_THRESHOLD
                if (extremePitch || extremeYaw || extremeRoll) {
                    Timber.d("Extreme head pose detected")
                    conditionFailedWithReasonAndTimeout(LookStraight)
                    // Don't reset progress here, because the user might be trying some active
                    // liveness tasks
                    return@addOnSuccessListener
                }

                // Reject closed eyes. But if it's null, assume eyes are open
                val isLeftEyeClosed = (face.leftEyeOpenProbability ?: 1f) < 0.3
                val isRightEyeClosed = (face.rightEyeOpenProbability ?: 1f) < 0.3
                // && instead of || for cases where the user has e.g. an eyepatch
                if (isLeftEyeClosed && isRightEyeClosed) {
                    Timber.d("Closed eyes detected")
                    conditionFailedWithReasonAndTimeout(LookStraight)
                    return@addOnSuccessListener
                }

                // We only run the image quality model on the selfie capture because the liveness
                // task requires a turned head, which receives a lower score from the model

                // Image Quality Model Inference
                // Model input: nx120x120x3 - n images, each cropped to face bounding box
                // Model output: nx2 - n images, each with 2 probabilities
                // 1st column is the actual quality. 2nd column is 1-(1st_column)

                // NB! Model is trained on *face mesh* crop, not face bounding box crop

                val input = TensorImage(DataType.FLOAT32).apply {
                    val modelInputBmp = Bitmap.createBitmap(
                        bmp,
                        contourRect.left,
                        contourRect.top,
                        contourRect.width(),
                        contourRect.height(),
                        // NB! input is not guaranteed to be square, so scale might squish the image
                    ).scale(modelInputSize[1], modelInputSize[2], false)
                    load(modelInputBmp)
                }
                val outputs = selfieQualityModel.process(input.tensorBuffer)
                val output = outputs.outputFeature0AsTensorBuffer.floatArray.firstOrNull() ?: run {
                    Timber.w("No image quality output")
                    return@addOnSuccessListener
                }
                selfieQualityHistory.add(output)
                if (selfieQualityHistory.size > SELFIE_QUALITY_HISTORY_LENGTH) {
                    // We should only ever exceed history length by 1
                    selfieQualityHistory.removeAt(0)
                }

                val averageFaceQuality = selfieQualityHistory.average()

                if (averageFaceQuality < FACE_QUALITY_THRESHOLD) {
                    // We don't want to reset the history here, since the model output is noisy, so
                    // don't use the helper function
                    Timber.d("Face quality not met ($averageFaceQuality)")
                    _uiState.update {
                        it.copy(selfieState = SelfieState.Analyzing(PoorImageQuality))
                    }
                    return@addOnSuccessListener
                }
                if (shouldUseActiveLiveness) {
                    _uiState.update {
                        it.copy(selfieState = SelfieState.Analyzing(activeLiveness.selfieHint))
                    }
                }
                selfieCameraOrientation = imageProxy.imageInfo.rotationDegrees
                lastAutoCaptureTimeMs = System.currentTimeMillis()
                // local variable is for null type safety purposes
                val selfieFile = createSelfieFile(userId)
                this.selfieFile = selfieFile
                Timber.v("Capturing selfie image to $selfieFile")
                postProcessImageBitmap(
                    bitmap = bmp,
                    file = selfieFile,
                    compressionQuality = 80,
                    resizeLongerDimensionTo = SELFIE_IMAGE_SIZE,
                )
                return@addOnSuccessListener
            }

            if (shouldUseActiveLiveness) {
                if (!activeLiveness.doesFaceMeetCurrentActiveLivenessTask(face)) {
                    return@addOnSuccessListener
                }
                activeLiveness.markCurrentDirectionSatisfied()
            }

            val livenessFile = createLivenessFile(userId)
            Timber.v("Capturing liveness image to $livenessFile")
            postProcessImageBitmap(
                bitmap = bmp,
                file = livenessFile,
                compressionQuality = 80,
                resizeLongerDimensionTo = LIVENESS_IMAGE_SIZE,
            )
            livenessFiles.add(livenessFile)

            if (shouldUseActiveLiveness) {
                if (!activeLiveness.isFinished) {
                    _uiState.update {
                        it.copy(selfieState = SelfieState.Analyzing(activeLiveness.selfieHint))
                    }
                    return@addOnSuccessListener
                }
            } else {
                if (livenessFiles.size < NUM_LIVENESS_IMAGES) {
                    _uiState.update { it.copy(selfieState = SelfieState.Analyzing(Smile)) }
                    return@addOnSuccessListener
                }
            }

            shouldAnalyzeImages = false
            setCameraFacingMetadata(camSelector)
            val proxy = { e: Throwable ->
                when {
                    e is IOException -> {
                        Timber.w(e, "Received IOException, asking user to retry")
                        _uiState.update { it.copy(selfieState = SelfieState.Error(e)) }
                    }

                    e is HttpException && e.code() in 500..599 -> {
                        val message = "Received 5xx error, asking user to retry"
                        Timber.w(e, message)
                        SmileIDCrashReporting.hub.addBreadcrumb(message)
                        _uiState.update { it.copy(selfieState = SelfieState.Error(e)) }
                    }

                    else -> onResult(SmileIDResult.Error(e))
                }
            }
            viewModelScope.launch(getExceptionHandler(proxy)) {
                var done = false
                // Start submitting the job right away, but show the spinner after a small delay
                // to make it feel like the API call is a bit faster
                awaitAll(
                    async {
                        val apiResponse = submitJob(selfieFile)
                        done = true
                        _uiState.update { it.copy(selfieState = SelfieState.Success(apiResponse)) }
                        // Delay to ensure the completion icon is shown for a little bit
                        delay(COMPLETED_DELAY_MS)
                        val result = SmartSelfieResult(selfieFile, livenessFiles, apiResponse)
                        onResult(SmileIDResult.Success(result))
                    },
                    async {
                        delay(LOADING_INDICATOR_DELAY_MS)
                        if (!done) {
                            _uiState.update { it.copy(selfieState = SelfieState.Processing) }
                        }
                    },
                )
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error analyzing image")
            SmileIDCrashReporting.hub.addBreadcrumb("Error analyzing image")
            onResult(SmileIDResult.Error(exception))
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private suspend fun submitJob(selfieFile: File): SmartSelfieResponse {
        metadata.add(Metadatum.SelfieCaptureDuration(metadataTimerStart.elapsedNow()))
        return if (isEnroll) {
            SmileID.api.doSmartSelfieEnrollment(
                userId = userId,
                selfieImage = selfieFile,
                livenessImages = livenessFiles,
                allowNewEnroll = allowNewEnroll,
                partnerParams = extraPartnerParams,
                failureReason = FailureReason(activeLivenessTimedOut = forcedFailureTimerExpired),
                metadata = metadata.asNetworkRequest(),
            )
        } else {
            SmileID.api.doSmartSelfieAuthentication(
                userId = userId,
                selfieImage = selfieFile,
                livenessImages = livenessFiles,
                partnerParams = extraPartnerParams,
                failureReason = FailureReason(activeLivenessTimedOut = forcedFailureTimerExpired),
                metadata = metadata.asNetworkRequest(),
            )
        }
    }

    /**
     * The retry button is displayed only when the error is due to IO issues (network or file) or
     * unexpected 5xx. In these cases, we restart the process entirely, mainly to cater for the
     * file IO scenario.
     */
    fun onRetry() {
        metadata.removeAll { it is Metadatum.SelfieCaptureDuration }
        metadata.removeAll { it is Metadatum.SelfieImageOrigin }
        resetCaptureProgress(SearchingForFace)
        forcedFailureTimerExpired = false
        startStrictModeTimerIfNecessary()
        shouldAnalyzeImages = true
    }

    private fun setCameraFacingMetadata(camSelector: CamSelector) {
        metadata.removeAll { it is Metadatum.SelfieImageOrigin }
        when (camSelector) {
            CamSelector.Front -> metadata.add(Metadatum.SelfieImageOrigin(FrontCamera))
            CamSelector.Back -> metadata.add(Metadatum.SelfieImageOrigin(BackCamera))
        }
    }

    /**
     * If the user hasn't satisfied capture conditions yet, then immediately update the UI with
     * feedback. Otherwise, we will maintain the currently displayed directive *as long as just the
     * face is visible*! e.g. we will show "Look left" even if the face is too far/needs to show
     * some other feedback. This is to prevent the user from being overwhelmed with feedback, but
     * comes with the downside that they may no longer get accurate feedback.
     * If the face is not detected for [NO_FACE_RESET_DELAY_MS], then progress is reset.
     */
    private fun conditionFailedWithReasonAndTimeout(reason: SelfieHint) {
        if (selfieFile == null) {
            _uiState.update { it.copy(selfieState = SelfieState.Analyzing(reason)) }
        } else if (System.currentTimeMillis() - lastValidFaceDetectTime > NO_FACE_RESET_DELAY_MS) {
            resetCaptureProgress(reason)
        }
        // Otherwise we swallow the failure reason.
        // Subsequently, in future invocations, either the face *does* satisfy all the conditions,
        // OR, NO_FACE_RESET_DELAY_MS time elapses, and we reset progress at that point
    }

    private fun resetCaptureProgress(reason: SelfieHint) {
        _uiState.update { it.copy(selfieState = SelfieState.Analyzing(reason)) }
        selfieQualityHistory.clear()
        livenessFiles.removeAll { it.delete() }
        selfieFile?.delete()
        selfieFile = null
        activeLiveness.restart()
    }
}
