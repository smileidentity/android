package com.smileidentity.viewmodel

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.metadata.asNetworkRequest
import com.smileidentity.metadata.device.model
import com.smileidentity.metadata.models.LivenessType
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.SelfieImageOriginValue.BackCamera
import com.smileidentity.metadata.models.SelfieImageOriginValue.FrontCamera
import com.smileidentity.metadata.updaters.DeviceOrientationMetadata
import com.smileidentity.models.v2.FailureReason
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
import com.smileidentity.viewmodel.SelfieHint.EnsureDeviceUpright
import com.smileidentity.viewmodel.SelfieHint.EnsureEntireFaceVisible
import com.smileidentity.viewmodel.SelfieHint.LookStraight
import com.smileidentity.viewmodel.SelfieHint.MoveBack
import com.smileidentity.viewmodel.SelfieHint.MoveCloser
import com.smileidentity.viewmodel.SelfieHint.NeedLight
import com.smileidentity.viewmodel.SelfieHint.OnlyOneFace
import com.smileidentity.viewmodel.SelfieHint.SearchingForFace
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
import retrofit2.HttpException
import timber.log.Timber

/*
This is used only when NOT in strict mode. In strict mode, the number of images is determined
by the liveness task
 */
const val VIEWFINDER_SCALE = 1.3f
private const val COMPLETED_DELAY_MS = 1500L
private const val FORCED_FAILURE_TIMEOUT_MS = 120_000L
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
private const val SELFIE_IMAGE_SIZE = 640

sealed interface SelfieState {
    data class Analyzing(val hint: SelfieHint) : SelfieState
    data object Processing : SelfieState
    data class Error(val throwable: Throwable) : SelfieState
    data class Success(
        val result: SmartSelfieResponse?,
        val selfieFile: File,
        val livenessFiles: List<File>,
    ) : SelfieState
}

enum class SelfieHint(@StringRes val text: Int) {
    NeedLight(text = R.string.si_smart_selfie_enhanced_directive_need_more_light),
    SearchingForFace(text = R.string.si_smart_selfie_enhanced_directive_place_entire_head_in_frame),
    MoveBack(text = R.string.si_smart_selfie_enhanced_directive_move_back),
    MoveCloser(text = R.string.si_smart_selfie_enhanced_directive_move_closer),
    LookLeft(text = R.string.si_smart_selfie_enhanced_directive_look_left),
    LookRight(text = R.string.si_smart_selfie_enhanced_directive_look_right),
    LookUp(text = R.string.si_smart_selfie_enhanced_directive_look_up),
    EnsureDeviceUpright(text = R.string.si_smart_selfie_enhanced_directive_ensure_device_upright),
    OnlyOneFace(text = R.string.si_smart_selfie_enhanced_directive_place_entire_head_in_frame),
    EnsureEntireFaceVisible(
        text = R.string.si_smart_selfie_enhanced_directive_place_entire_head_in_frame,
    ),
    PoorImageQuality(text = R.string.si_smart_selfie_enhanced_directive_need_more_light),
    LookStraight(text = R.string.si_smart_selfie_enhanced_directive_place_entire_head_in_frame),
}

data class SmartSelfieV2UiState(
    val topProgress: Float = 0F,
    val rightProgress: Float = 0F,
    val leftProgress: Float = 0F,
    val selfieFile: File? = null,
    val selfieState: SelfieState = SelfieState.Analyzing(SearchingForFace),
)

@kotlin.OptIn(FlowPreview::class)
class SmartSelfieEnhancedViewModel(
    private val userId: String,
    private val isEnroll: Boolean,
    private val metadata: MutableList<Metadatum>,
    private val allowNewEnroll: Boolean? = null,
    private val skipApiSubmission: Boolean,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    private val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().apply {
            enableTracking()
            setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        }.build(),
    ),
    private val onResult: SmileIDCallback<SmartSelfieResult>,
) : ViewModel() {
    private val activeLiveness = ActiveLivenessTask { leftProgress, rightProgress, topProgress ->
        _uiState.update {
            it.copy(
                leftProgress = leftProgress,
                rightProgress = rightProgress,
                topProgress = topProgress,
            )
        }
    }

    private val _uiState = MutableStateFlow(SmartSelfieV2UiState())
    val uiState = _uiState.asStateFlow().sample(500).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = _uiState.value,
    )
    private var faceTrackingId: Int? = null
    private val livenessFiles = mutableListOf<File>()
    private var selfieFile: File? = null
    private var lastAutoCaptureTimeMs = 0L
    private var lastValidFaceDetectTime = 0L
    private var shouldAnalyzeImages = true
    private var selfieCameraOrientation: Int? = null

    // Remove this line entirely
    private var forcedFailureTimerExpired = false
    private val shouldUseActiveLiveness: Boolean get() = !forcedFailureTimerExpired
    private var captureDuration = TimeSource.Monotonic.markNow()
    private var hasRecordedOrientationAtCaptureStart = false
    private var selfieCaptureRetries = 0
    private var networkRetries = 0

    init {
        startStrictModeTimerIfNecessary()
    }

    private fun isPortraitOrientation(degrees: Int): Boolean = degrees == 270

    /**
     * In strict mode, the user has a certain amount of time to finish the active liveness task. If
     * the user exceeds this time limit, the job will be explicitly failed by setting a flag on the
     * API request. Static liveness images will be captured for this
     */
    private fun startStrictModeTimerIfNecessary() {
        viewModelScope.launch {
            delay(FORCED_FAILURE_TIMEOUT_MS)
            val selfieState = uiState.value.selfieState
            // These 2 conditions should theoretically both be true at the same time
            if (!activeLiveness.isFinished && selfieState is SelfieState.Analyzing) {
                SmileIDCrashReporting.scopes.addBreadcrumb("Strict Mode force fail timer expired")
                Timber.d("Strict Mode forced failure timer expired")
                forcedFailureTimerExpired = true
                resetCaptureProgress(LookStraight)
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
        /*
         At the start of the capture, we record the device orientation and start the capture
         duration timer.
         */
        if (!hasRecordedOrientationAtCaptureStart) {
            try {
                DeviceOrientationMetadata.shared.storeDeviceOrientation()
            } catch (_: UninitializedPropertyAccessException) {
                /*
                In case .shared isn't initialised it throws the above exception. Given that the
                device orientation is only metadata we ignore it and take no action.
                 */
            }
            hasRecordedOrientationAtCaptureStart = true
            captureDuration = TimeSource.Monotonic.markNow()
        }

        val elapsedTimeSinceCaptureMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        val enoughTimeHasPassed = elapsedTimeSinceCaptureMs > INTRA_IMAGE_MIN_DELAY_MS
        if (!enoughTimeHasPassed || !shouldAnalyzeImages) {
            imageProxy.close()
            return
        }

        val image = imageProxy.image ?: run {
            Timber.w("ImageProxy has no image")
            SmileIDCrashReporting.scopes.addBreadcrumb("ImageProxy has no image")
            imageProxy.close()
            return
        }

        // We want to hold the orientation on portrait only :)
        val currentOrientation = imageProxy.imageInfo.rotationDegrees
        if (!isPortraitOrientation(currentOrientation)) {
            val message = "Camera orientation changed. Resetting progress"
            Timber.d(message)
            SmileIDCrashReporting.scopes.addBreadcrumb(message)
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

            // Ensure trackingId is available
            val currentTrackingId = face.trackingId
            if (currentTrackingId == null) {
                conditionFailedWithReasonAndTimeout(OnlyOneFace)
                return@addOnSuccessListener
            }

            // First time seeing a face — store the trackingId, else reset the capture
            if (faceTrackingId == null) {
                faceTrackingId = currentTrackingId
            } else if (faceTrackingId != currentTrackingId) {
                conditionFailedWithReasonAndTimeout(OnlyOneFace)
                livenessFiles.removeAll { it.delete() }
                selfieFile?.delete()
                selfieFile = null
                faceTrackingId = null
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

            if (!activeLiveness.isFinished) {
                _uiState.update {
                    it.copy(selfieState = SelfieState.Analyzing(activeLiveness.selfieHint))
                }
                return@addOnSuccessListener
            }

            /*
             At the end of the capture, we record the device orientation and capture duration
             */
            try {
                DeviceOrientationMetadata.shared.storeDeviceOrientation()
            } catch (_: UninitializedPropertyAccessException) {
                /*
                In case .shared isn't initialised it throws the above exception. Given that the
                device orientation is only metadata we ignore it and take no action.
                 */
            }
            metadata.add(Metadatum.SelfieCaptureDuration(captureDuration.elapsedNow()))
            metadata.add(Metadatum.ActiveLivenessType(LivenessType.HeadPose))
            metadata.add(Metadatum.SelfieCaptureRetries(selfieCaptureRetries))

            try {
                DeviceOrientationMetadata.shared.storeDeviceMovement()
            } catch (_: UninitializedPropertyAccessException) {
                /*
                In case .shared isn't initialised it throws the above exception. Given that the
                device movement is only metadata we ignore it and take no action.
                 */
            }

            shouldAnalyzeImages = false
            setCameraFacingMetadata(camSelector)

            if (skipApiSubmission) {
                onSkipApiSubmission(selfieFile)
                return@addOnSuccessListener
            } else {
                /*
                 We add the network retries to the metadata here, because of the way we implement
                 the biometric kyc and document verification which is calling the selfie component
                 with skip api submission. If it is added before the skip api submission, it will be
                 added to the metadata twice.
                 */
                metadata.add(Metadatum.NetworkRetries(networkRetries))

                val proxy = { e: Throwable ->
                    when {
                        e is IOException -> {
                            Timber.w(e, "Received IOException, asking user to retry")
                        }

                        e is HttpException && e.code() in 500..599 -> {
                            val message = "Received 5xx error, asking user to retry"
                            Timber.w(e, message)
                            SmileIDCrashReporting.scopes.addBreadcrumb(message)
                        }
                    }
                    _uiState.update { it.copy(selfieState = SelfieState.Error(e)) }
                }
                viewModelScope.launch(getExceptionHandler(proxy)) {
                    Timber.d("viewModelScope.launch started")
                    // Start submitting the job right away, but show the spinner after a small delay
                    // to make it feel like the API call is a bit faster
                    var done = false
                    awaitAll(
                        async {
                            val apiResponse = submitJob(selfieFile)
                            done = true
                            _uiState.update {
                                it.copy(
                                    selfieState = SelfieState.Success(
                                        result = apiResponse,
                                        selfieFile = selfieFile,
                                        livenessFiles = livenessFiles,
                                    ),
                                    selfieFile = selfieFile,
                                )
                            }

                            networkRetries = 0
                            metadata.removeAll { it is Metadatum.NetworkRetries }

                            // Delay to ensure the completion icon is shown for a little bit
                            delay(COMPLETED_DELAY_MS)
                            val result = SmartSelfieResult(
                                selfieFile = selfieFile,
                                livenessFiles = livenessFiles,
                                apiResponse = apiResponse,
                            )
                            onResult(SmileIDResult.Success(result))
                        },
                        async {
                            delay(LOADING_INDICATOR_DELAY_MS)
                            if (!done) {
                                _uiState.update {
                                    it.copy(
                                        selfieFile = selfieFile,
                                        selfieState = SelfieState.Processing,
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error analyzing image")
            SmileIDCrashReporting.scopes.addBreadcrumb("Error analyzing image")
            onResult(SmileIDResult.Error(exception))
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private fun onSkipApiSubmission(selfieFile: File) {
        val result = SmartSelfieResult(
            selfieFile = selfieFile,
            livenessFiles = livenessFiles,
            apiResponse = null,
        )
        onResult(SmileIDResult.Success(result))
    }

    private suspend fun submitJob(selfieFile: File): SmartSelfieResponse = if (isEnroll) {
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

    /**
     * The retry button is displayed only when the error is due to IO issues (network or file) or
     * unexpected 5xx. In these cases, we restart the process entirely, mainly to cater for the
     * file IO scenario.
     */
    fun onRetry() {
        metadata.removeAll { it is Metadatum.CameraName }
        metadata.removeAll { it is Metadatum.SelfieCaptureDuration }
        metadata.removeAll { it is Metadatum.SelfieImageOrigin }
        metadata.removeAll { it is Metadatum.DeviceOrientation }
        resetCaptureProgress(SearchingForFace)
        forcedFailureTimerExpired = false
        startStrictModeTimerIfNecessary()
        selfieCaptureRetries++
        networkRetries++
        shouldAnalyzeImages = true
        hasRecordedOrientationAtCaptureStart = false
    }

    private fun setCameraFacingMetadata(camSelector: CamSelector) {
        metadata.removeAll { it is Metadatum.SelfieImageOrigin }
        metadata.removeAll { it is Metadatum.CameraName }
        when (camSelector) {
            CamSelector.Front -> {
                metadata.add(Metadatum.SelfieImageOrigin(FrontCamera))
                metadata.add(Metadatum.CameraName("""$model (Front Camera)"""))
            }

            CamSelector.Back -> {
                metadata.add(Metadatum.SelfieImageOrigin(BackCamera))
                metadata.add(Metadatum.CameraName("""$model (Back Camera)"""))
            }
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
        livenessFiles.removeAll { it.delete() }
        selfieFile?.delete()
        selfieFile = null
        activeLiveness.restart()
    }
}
