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
import com.smileidentity.models.v2.SmartSelfieResponse
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
import com.smileidentity.viewmodel.SelfieHint.EnsureEntireFaceVisible
import com.smileidentity.viewmodel.SelfieHint.LookStraight
import com.smileidentity.viewmodel.SelfieHint.MoveBack
import com.smileidentity.viewmodel.SelfieHint.MoveCloser
import com.smileidentity.viewmodel.SelfieHint.NeedLight
import com.smileidentity.viewmodel.SelfieHint.OnlyOneFace
import com.smileidentity.viewmodel.SelfieHint.PoorImageQuality
import com.smileidentity.viewmodel.SelfieHint.SearchingForFace
import com.smileidentity.viewmodel.SelfieHint.Smile
import java.io.File
import java.io.IOException
import kotlin.math.absoluteValue
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
private const val NUM_LIVENESS_IMAGES = 8
private const val LIVENESS_IMAGE_SIZE = 320
private const val SELFIE_IMAGE_SIZE = 640
const val VIEWFINDER_SCALE = 1.3f

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
    val SELFIE_QUALITY_HISTORY_LENGTH: Float,
    val INTRA_IMAGE_MIN_DELAY_MS: Float,
    val NO_FACE_RESET_DELAY_MS: Float,
    val FACE_QUALITY_THRESHOLD: Float,
    val MIN_FACE_FILL_THRESHOLD: Float,
    val MAX_FACE_FILL_THRESHOLD: Float,
    val LUMINANCE_THRESHOLD: Float,
    val MAX_FACE_PITCH_THRESHOLD: Float,
    val MAX_FACE_YAW_THRESHOLD: Float,
    val MAX_FACE_ROLL_THRESHOLD: Float,
    val FORCED_FAILURE_TIMEOUT_MS: Float,
    val LOADING_INDICATOR_DELAY_MS: Float,
    val COMPLETED_DELAY_MS: Float,
    val LIVENESS_STABILITY_TIME_MS: Float,
    val ORTHOGONAL_ANGLE_BUFFER: Float,
    val MIDWAY_LR_ANGLE_MIN: Float,
    val MIDWAY_LR_ANGLE_MAX: Float,
    val END_LR_ANGLE_MIN: Float,
    val END_LR_ANGLE_MAX: Float,
    val MIDWAY_UP_ANGLE_MIN: Float,
    val MIDWAY_UP_ANGLE_MAX: Float,
    val END_UP_ANGLE_MIN: Float,
    val END_UP_ANGLE_MAX: Float,
    val IGNORE_FACES_SMALLER_THAN: Float,
    val headRoll: Float,
    val headYaw: Float,
    val headPitch: Float,
    val selfieQuality: Float,
)

@kotlin.OptIn(FlowPreview::class)
class SmartSelfieV2ViewModel(
    private val userId: String,
    private val isEnroll: Boolean,
    private val useStrictMode: Boolean,
    private val selfieQualityModel: SelfieQualityModel,
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
    // PARAMETER DEBUGGING
    private var selfieQualityHistoryLength = 7
    private var intraImageMinDelayMs = 250
    private var noFaceResetDelayMs = 500
    private var faceQualityThreshold = 0.5f
    private var minFaceFillThreshold = 0.1f
    private var maxFaceFillThreshold = 0.3f
    private var luminanceThreshold = 50
    private var maxFacePitchThreshold = 30
    private var maxFaceYawThreshold = 15
    private var maxFaceRollThreshold = 30
    private var forcedFailureTimeoutMs = 60_000L
    private var loadingIndicatorDelayMs = 200L
    private var completedDelayMs = 2000L
    private var ignoreFacesSmallerThan = 0.03f

    private val activeLiveness = ActiveLivenessTask()

    private val _uiState = MutableStateFlow(
        SmartSelfieV2UiState(
            SELFIE_QUALITY_HISTORY_LENGTH = selfieQualityHistoryLength.toFloat(),
            INTRA_IMAGE_MIN_DELAY_MS = intraImageMinDelayMs.toFloat(),
            NO_FACE_RESET_DELAY_MS = noFaceResetDelayMs.toFloat(),
            FACE_QUALITY_THRESHOLD = faceQualityThreshold,
            MIN_FACE_FILL_THRESHOLD = minFaceFillThreshold,
            MAX_FACE_FILL_THRESHOLD = maxFaceFillThreshold,
            LUMINANCE_THRESHOLD = luminanceThreshold.toFloat(),
            MAX_FACE_PITCH_THRESHOLD = maxFacePitchThreshold.toFloat(),
            MAX_FACE_YAW_THRESHOLD = maxFaceYawThreshold.toFloat(),
            MAX_FACE_ROLL_THRESHOLD = maxFaceRollThreshold.toFloat(),
            FORCED_FAILURE_TIMEOUT_MS = forcedFailureTimeoutMs.toFloat(),
            LOADING_INDICATOR_DELAY_MS = loadingIndicatorDelayMs.toFloat(),
            COMPLETED_DELAY_MS = completedDelayMs.toFloat(),
            LIVENESS_STABILITY_TIME_MS = activeLiveness.livenessStabilityTimeMs.toFloat(),
            ORTHOGONAL_ANGLE_BUFFER = activeLiveness.orthogonalAngleBuffer,
            MIDWAY_LR_ANGLE_MIN = activeLiveness.midwayLrAngleMin,
            MIDWAY_LR_ANGLE_MAX = activeLiveness.midwayLrAngleMax,
            END_LR_ANGLE_MIN = activeLiveness.endLrAngleMin,
            END_LR_ANGLE_MAX = activeLiveness.endLrAngleMax,
            MIDWAY_UP_ANGLE_MIN = activeLiveness.midwayUpAngleMin,
            MIDWAY_UP_ANGLE_MAX = activeLiveness.midwayUpAngleMax,
            END_UP_ANGLE_MIN = activeLiveness.endUpAngleMin,
            END_UP_ANGLE_MAX = activeLiveness.endUpAngleMax,
            IGNORE_FACES_SMALLER_THAN = ignoreFacesSmallerThan,
            headRoll = 0f,
            headYaw = 0f,
            headPitch = 0f,
            selfieQuality = 0f,
        ),
    )
    val uiState = _uiState.asStateFlow().sample(250).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        _uiState.value,
    )
    private val livenessFiles = mutableListOf<File>()
    private var selfieFile: File? = null
    private var lastAutoCaptureTimeMs = 0L
    private var lastValidFaceDetectTime = 0L
    private var shouldAnalyzeImages = true
    private val modelInputSize = intArrayOf(1, 120, 120, 3)
    private val selfieQualityHistory = mutableListOf<Float>()
    private var forcedFailureTimerExpired = false
    private val shouldUseActiveLiveness: Boolean get() = useStrictMode && !forcedFailureTimerExpired

    init {
        // startStrictModeTimerIfNecessary()
    }

    /**
     * In strict mode, the user has a certain amount of time to finish the active liveness task. If
     * the user exceeds this time limit, the job will be explicitly failed by setting a flag on the
     * API request. Static liveness images will be captured for this
     */
    private fun startStrictModeTimerIfNecessary() {
        if (useStrictMode) {
            viewModelScope.launch {
                delay(forcedFailureTimeoutMs)
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
    fun analyzeImage(imageProxy: ImageProxy) {
        val elapsedTimeSinceCaptureMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        val enoughTimeHasPassed = elapsedTimeSinceCaptureMs > intraImageMinDelayMs
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

        // YUV_420_888 is the format produced by CameraX and needed for Luminance calculation
        check(imageProxy.format == YUV_420_888) { "Unsupported format: ${imageProxy.format}" }
        val luminance = calculateLuminance(imageProxy)
        if (luminance < luminanceThreshold) {
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
                    it.boundingBox.area / inputImage.area.toFloat() > ignoreFacesSmallerThan
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
            _uiState.update {
                it.copy(
                    headRoll = face.headEulerAngleZ,
                    headYaw = face.headEulerAngleY,
                    headPitch = face.headEulerAngleX,
                )
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
            if (faceFillRatio < minFaceFillThreshold) {
                Timber.d("Face not close enough to camera")
                conditionFailedWithReasonAndTimeout(MoveCloser)
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            if (faceFillRatio > maxFaceFillThreshold) {
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
                val extremePitch = face.headEulerAngleX.absoluteValue > maxFacePitchThreshold
                val extremeYaw = face.headEulerAngleY.absoluteValue > maxFaceYawThreshold
                val extremeRoll = face.headEulerAngleZ.absoluteValue > maxFaceRollThreshold
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
                _uiState.update { it.copy(selfieQuality = output) }
                selfieQualityHistory.add(output)
                if (selfieQualityHistory.size > selfieQualityHistoryLength) {
                    // We should only ever exceed history length by 1
                    selfieQualityHistory.removeAt(0)
                }

                val averageFaceQuality = selfieQualityHistory.average()

                if (averageFaceQuality < faceQualityThreshold) {
                    // We don't want to reset the history here, since the model output is noisy, so
                    // don't use the helper function
                    Timber.d("Face quality not met ($averageFaceQuality)")
                    _uiState.update {
                        it.copy(selfieState = SelfieState.Analyzing(PoorImageQuality))
                    }
                    return@addOnSuccessListener
                }
                _uiState.update {
                    it.copy(selfieState = SelfieState.Analyzing(activeLiveness.selfieHint))
                }
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
                        delay(completedDelayMs)
                        val result = SmartSelfieResult(selfieFile, livenessFiles, apiResponse)
                        onResult(SmileIDResult.Success(result))
                    },
                    async {
                        delay(loadingIndicatorDelayMs)
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

    // activeLivenessDetails = ActiveLivenessDetails(orderedFaceDirections = activeLiveness.orderedFaceDirections, forceFailure = forcedFailureTimerExpired),
    private suspend fun submitJob(selfieFile: File): SmartSelfieResponse {
        return if (isEnroll) {
            SmileID.api.doSmartSelfieEnrollment(
                userId = userId,
                selfieImage = selfieFile,
                livenessImages = livenessFiles,
                allowNewEnroll = allowNewEnroll,
                partnerParams = extraPartnerParams,
                metadata = null,
            )
        } else {
            SmileID.api.doSmartSelfieAuthentication(
                userId = userId,
                selfieImage = selfieFile,
                livenessImages = livenessFiles,
                partnerParams = extraPartnerParams,
                metadata = null,
            )
        }
    }

    fun stop() {
        shouldAnalyzeImages = false
        resetCaptureProgress(SearchingForFace)
        forcedFailureTimerExpired = false
    }

    fun start() {
        shouldAnalyzeImages = true
        startStrictModeTimerIfNecessary()
    }

    /**
     * The retry button is displayed only when the error is due to IO issues (network or file) or
     * unexpected 5xx. In these cases, we restart the process entirely, mainly to cater for the
     * file IO scenario.
     */
    fun onRetry() {
        resetCaptureProgress(SearchingForFace)
        forcedFailureTimerExpired = false
        startStrictModeTimerIfNecessary()
        shouldAnalyzeImages = true
    }

    /**
     * If the user hasn't satisfied capture conditions yet, then immediately update the UI with
     * feedback. Otherwise, we will maintain the currently displayed directive *as long as just the
     * face is visible*! e.g. we will show "Look left" even if the face is too far/needs to show
     * some other feedback. This is to prevent the user from being overwhelmed with feedback, but
     * comes with the downside that they may no longer get accurate feedback.
     * If the face is not detected for [noFaceResetDelayMs], then progress is reset.
     */
    private fun conditionFailedWithReasonAndTimeout(reason: SelfieHint) {
        if (selfieFile == null) {
            _uiState.update { it.copy(selfieState = SelfieState.Analyzing(reason)) }
        } else if (System.currentTimeMillis() - lastValidFaceDetectTime > noFaceResetDelayMs) {
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

    // Parameter Debugging
    fun onSelfieQualityHistoryLengthUpdated(value: Float) {
        selfieQualityHistoryLength = value.toInt()
        _uiState.update { it.copy(SELFIE_QUALITY_HISTORY_LENGTH = value) }
    }

    fun onIntraImageMinDelayMsUpdated(value: Float) {
        intraImageMinDelayMs = value.toInt()
        _uiState.update { it.copy(INTRA_IMAGE_MIN_DELAY_MS = value) }
    }

    fun onNoFaceResetDelayMsUpdated(value: Float) {
        noFaceResetDelayMs = value.toInt()
        _uiState.update { it.copy(NO_FACE_RESET_DELAY_MS = value) }
    }

    fun onFaceQualityThresholdUpdated(value: Float) {
        faceQualityThreshold = value
        _uiState.update { it.copy(FACE_QUALITY_THRESHOLD = value) }
    }

    fun onMinFaceFillThresholdUpdated(value: Float) {
        minFaceFillThreshold = value
        _uiState.update { it.copy(MIN_FACE_FILL_THRESHOLD = value) }
    }

    fun onMaxFaceFillThresholdUpdated(value: Float) {
        maxFaceFillThreshold = value
        _uiState.update { it.copy(MAX_FACE_FILL_THRESHOLD = value) }
    }

    fun onLuminanceThresholdUpdated(value: Float) {
        luminanceThreshold = value.toInt()
        _uiState.update { it.copy(LUMINANCE_THRESHOLD = value) }
    }

    fun onMaxFacePitchThresholdUpdated(value: Float) {
        maxFacePitchThreshold = value.toInt()
        _uiState.update { it.copy(MAX_FACE_PITCH_THRESHOLD = value) }
    }

    fun onMaxFaceYawThresholdUpdated(value: Float) {
        maxFaceYawThreshold = value.toInt()
        _uiState.update { it.copy(MAX_FACE_YAW_THRESHOLD = value) }
    }

    fun onMaxFaceRollThresholdUpdated(value: Float) {
        maxFaceRollThreshold = value.toInt()
        _uiState.update { it.copy(MAX_FACE_ROLL_THRESHOLD = value) }
    }

    fun onForcedFailureTimeoutMsUpdated(value: Float) {
        forcedFailureTimeoutMs = value.toLong()
        _uiState.update { it.copy(FORCED_FAILURE_TIMEOUT_MS = value) }
    }

    fun onLoadingIndicatorDelayMsUpdated(value: Float) {
        loadingIndicatorDelayMs = value.toLong()
        _uiState.update { it.copy(LOADING_INDICATOR_DELAY_MS = value) }
    }

    fun onCompletedDelayMsUpdated(value: Float) {
        completedDelayMs = value.toLong()
        _uiState.update { it.copy(COMPLETED_DELAY_MS = value) }
    }

    fun onLivenessStabilityTimeMsUpdated(value: Float) {
        activeLiveness.livenessStabilityTimeMs = value.toLong()
        _uiState.update { it.copy(LIVENESS_STABILITY_TIME_MS = value) }
    }

    fun onOrthogonalAngleBufferUpdated(value: Float) {
        activeLiveness.orthogonalAngleBuffer = value
        _uiState.update { it.copy(ORTHOGONAL_ANGLE_BUFFER = value) }
    }

    fun onMidwayLrAngleMinUpdated(value: Float) {
        activeLiveness.midwayLrAngleMin = value
        _uiState.update { it.copy(MIDWAY_LR_ANGLE_MIN = value) }
    }

    fun onMidwayLrAngleMaxUpdated(value: Float) {
        activeLiveness.midwayLrAngleMax = value
        _uiState.update { it.copy(MIDWAY_LR_ANGLE_MAX = value) }
    }

    fun onEndLrAngleMinUpdated(value: Float) {
        activeLiveness.endLrAngleMin = value
        _uiState.update { it.copy(END_LR_ANGLE_MIN = value) }
    }

    fun onEndLrAngleMaxUpdated(value: Float) {
        activeLiveness.endLrAngleMax = value
        _uiState.update { it.copy(END_LR_ANGLE_MAX = value) }
    }

    fun onMidwayUpAngleMinUpdated(value: Float) {
        activeLiveness.midwayUpAngleMin = value
        _uiState.update { it.copy(MIDWAY_UP_ANGLE_MIN = value) }
    }

    fun onMidwayUpAngleMaxUpdated(value: Float) {
        activeLiveness.midwayUpAngleMax = value
        _uiState.update { it.copy(MIDWAY_UP_ANGLE_MAX = value) }
    }

    fun onEndUpAngleMinUpdated(value: Float) {
        activeLiveness.endUpAngleMin = value
        _uiState.update { it.copy(END_UP_ANGLE_MIN = value) }
    }

    fun onEndUpAngleMaxUpdated(value: Float) {
        activeLiveness.endUpAngleMax = value
        _uiState.update { it.copy(END_UP_ANGLE_MAX = value) }
    }

    fun onIgnoreFacesSmallerThanUpdated(value: Float) {
        _uiState.update { it.copy(IGNORE_FACES_SMALLER_THAN = value) }
    }
}
