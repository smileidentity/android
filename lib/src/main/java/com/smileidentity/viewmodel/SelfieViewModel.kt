package com.smileidentity.viewmodel

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.metadata.asNetworkRequest
import com.smileidentity.metadata.models.LivenessType
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.SelfieImageOriginValue.BackCamera
import com.smileidentity.metadata.models.SelfieImageOriginValue.FrontCamera
import com.smileidentity.metadata.updaters.DeviceOrientationMetadata
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.SmileIDException
import com.smileidentity.networking.doSmartSelfieAuthentication
import com.smileidentity.networking.doSmartSelfieEnrollment
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.FileType
import com.smileidentity.util.StringResource
import com.smileidentity.util.area
import com.smileidentity.util.createAuthenticationRequestFile
import com.smileidentity.util.createLivenessFile
import com.smileidentity.util.createPrepUploadFile
import com.smileidentity.util.createSelfieFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.getFileByType
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.handleOfflineJobFailure
import com.smileidentity.util.isNetworkFailure
import com.smileidentity.util.moveJobToSubmitted
import com.smileidentity.util.postProcessImageBitmap
import com.smileidentity.util.rotated
import com.ujizin.camposer.state.CamSelector
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private val UI_DEBOUNCE_DURATION = 250.milliseconds
private const val INTRA_IMAGE_MIN_DELAY_MS = 350
private const val NUM_LIVENESS_IMAGES = 7
private const val TOTAL_STEPS = NUM_LIVENESS_IMAGES + 1 // 7 B&W Liveness + 1 Color Selfie
private const val LIVENESS_IMAGE_SIZE = 320
private const val SELFIE_IMAGE_SIZE = 640
private const val NO_FACE_RESET_DELAY_MS = 3000
private const val FACE_ROTATION_THRESHOLD = 0.75f
private const val MIN_FACE_AREA_THRESHOLD = 0.15f
const val MAX_FACE_AREA_THRESHOLD = 0.30f
private const val SMILE_THRESHOLD = 0.8f

private val faceBoundingBoxQueue: ArrayDeque<Pair<Long, Rect>> = ArrayDeque()
private val FACE_STABILITY_WINDOW_MS = 3000L // 3 seconds
private val FACE_MOVEMENT_THRESHOLD_PX = 15 // acceptable pixel movement

data class SelfieUiState(
    val directive: SelfieDirective = SelfieDirective.InitialInstruction,
    val progress: Float = 0f,
    val selfieToConfirm: File? = null,
    val processingState: ProcessingState? = null,
    val errorMessage: StringResource = StringResource.ResId(R.string.si_processing_error_subtitle),
)

enum class SelfieDirective(@StringRes val displayText: Int) {
    InitialInstruction(R.string.si_smart_selfie_instructions),
    Capturing(R.string.si_smart_selfie_directive_capturing),
    EnsureFaceInFrame(R.string.si_smart_selfie_directive_unable_to_detect_face),
    HoldStill(R.string.si_smart_selfie_directive_hold_still),
    EnsureOnlyOneFace(R.string.si_smart_selfie_directive_multiple_faces),
    MoveCloser(R.string.si_smart_selfie_directive_face_too_far),
    MoveAway(R.string.si_smart_selfie_directive_face_too_close),
    Smile(R.string.si_smart_selfie_directive_smile),
}

class SelfieViewModel(
    private val isEnroll: Boolean,
    private val userId: String,
    private val jobId: String,
    private val allowNewEnroll: Boolean,
    private val skipApiSubmission: Boolean,
    private val metadata: MutableList<Metadatum>,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieUiState())

    // Debounce to avoid spamming SelfieDirective updates so that they can be read by the user
    @kotlin.OptIn(FlowPreview::class)
    val uiState = _uiState.asStateFlow().debounce(UI_DEBOUNCE_DURATION).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        SelfieUiState(),
    )
    var result: SmileIDResult<SmartSelfieResult>? = null
    private var faceTrackingId: Int? = null

    private val livenessFiles = mutableListOf<File>()
    private var selfieFile: File? = null
    private var lastAutoCaptureTimeMs = 0L
    private var previousHeadRotationX = Float.POSITIVE_INFINITY
    private var previousHeadRotationY = Float.POSITIVE_INFINITY
    private var previousHeadRotationZ = Float.POSITIVE_INFINITY

    @VisibleForTesting
    internal var shouldAnalyzeImages = true

    private val faceDetectorOptions = FaceDetectorOptions.Builder().apply {
        enableTracking()
        setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    }.build()
    private val faceDetector by lazy { FaceDetection.getClient(faceDetectorOptions) }

    private var captureDuration = TimeSource.Monotonic.markNow()
    private var hasRecordedOrientationAtCaptureStart = false
    private var selfieCaptureRetries = 0
    private var networkRetries = 0

    @OptIn(ExperimentalGetImage::class)
    internal fun analyzeImage(imageProxy: ImageProxy, camSelector: CamSelector) {
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

        val image = imageProxy.image
        val elapsedTimeMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        if (!shouldAnalyzeImages || elapsedTimeMs < INTRA_IMAGE_MIN_DELAY_MS || image == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            if (faces.isEmpty()) {
                _uiState.update { it.copy(directive = SelfieDirective.EnsureFaceInFrame) }
                // If no faces are detected for a while, reset the state
                if (elapsedTimeMs > NO_FACE_RESET_DELAY_MS) {
                    _uiState.update {
                        it.copy(
                            progress = 0f,
                            selfieToConfirm = null,
                            processingState = null,
                        )
                    }
                    livenessFiles.removeAll { it.delete() }
                    selfieFile?.delete()
                    selfieFile = null
                }
                return@addOnSuccessListener
            }

            // Ensure only 1 face is in frame
            if (faces.size > 1) {
                _uiState.update { it.copy(directive = SelfieDirective.EnsureOnlyOneFace) }
                return@addOnSuccessListener
            }

            val face = faces.first()

            // Ensure trackingId is available
            val currentTrackingId = face.trackingId
            if (currentTrackingId == null) {
                _uiState.update { it.copy(directive = SelfieDirective.EnsureOnlyOneFace) }
                return@addOnSuccessListener
            }

            // First time seeing a face — store the trackingId, else reset the capture
            if (faceTrackingId == null) {
                faceTrackingId = currentTrackingId
            } else if (faceTrackingId != currentTrackingId) {
                _uiState.update {
                    it.copy(
                        directive = SelfieDirective.EnsureOnlyOneFace,
                        progress = 0f,
                        selfieToConfirm = null,
                        processingState = null,
                    )
                }
                livenessFiles.removeAll { it.delete() }
                selfieFile?.delete()
                selfieFile = null
                faceTrackingId = null
                return@addOnSuccessListener
            }

            // Check that the corners of the face bounding box are within the inputImage
            val faceCornersInImage = face.boundingBox.left >= 0 &&
                face.boundingBox.right <= inputImage.width &&
                face.boundingBox.top >= 0 &&
                face.boundingBox.bottom <= inputImage.height
            if (!faceCornersInImage) {
                _uiState.update { it.copy(directive = SelfieDirective.EnsureFaceInFrame) }
                return@addOnSuccessListener
            }

            if (!isFaceStable(face = face.boundingBox)) {
                _uiState.update { it.copy(directive = SelfieDirective.HoldStill) }
                return@addOnSuccessListener
            }

            // Check that the face is close enough to the camera
            val faceFillRatio = (face.boundingBox.area / inputImage.area.toFloat())
            if (faceFillRatio < MIN_FACE_AREA_THRESHOLD) {
                _uiState.update { it.copy(directive = SelfieDirective.MoveCloser) }
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            if (faceFillRatio > MAX_FACE_AREA_THRESHOLD) {
                _uiState.update { it.copy(directive = SelfieDirective.MoveAway) }
                return@addOnSuccessListener
            }

            // Ask the user to start smiling partway through liveness images
            val isSmiling = (face.smilingProbability ?: 0f) > SMILE_THRESHOLD
            if (livenessFiles.size > NUM_LIVENESS_IMAGES / 2 && !isSmiling) {
                _uiState.update { it.copy(directive = SelfieDirective.Smile) }
                return@addOnSuccessListener
            }

            _uiState.update { it.copy(directive = SelfieDirective.Capturing) }

            // Perform the rotation checks *after* changing directive to Capturing -- we don't want
            // to explicitly tell the user to move their head
            if (!hasFaceRotatedEnough(face)) {
                Timber.v("Not enough face rotation between captures. Waiting...")
                return@addOnSuccessListener
            }
            previousHeadRotationX = face.headEulerAngleX
            previousHeadRotationY = face.headEulerAngleY
            previousHeadRotationZ = face.headEulerAngleZ

            // All conditions satisfied, capture the image

            val bitmap = imageProxy.toBitmap().rotated(imageProxy.imageInfo.rotationDegrees)
            lastAutoCaptureTimeMs = System.currentTimeMillis()
            if (livenessFiles.size < NUM_LIVENESS_IMAGES) {
                val livenessFile = createLivenessFile(jobId)
                Timber.v("Capturing liveness image to $livenessFile")
                postProcessImageBitmap(
                    bitmap = bitmap,
                    file = livenessFile,
                    compressionQuality = 80,
                    resizeLongerDimensionTo = LIVENESS_IMAGE_SIZE,
                )
                livenessFiles.add(livenessFile)
                _uiState.update { it.copy(progress = livenessFiles.size / TOTAL_STEPS.toFloat()) }
            } else {
                selfieFile = createSelfieFile(jobId)
                Timber.v("Capturing selfie image to $selfieFile")
                postProcessImageBitmap(
                    bitmap = bitmap,
                    file = selfieFile!!,
                    compressionQuality = 80,
                    resizeLongerDimensionTo = SELFIE_IMAGE_SIZE,
                )
                shouldAnalyzeImages = false
                setCameraFacingMetadata(camSelector)

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

                _uiState.update {
                    it.copy(
                        progress = 1f,
                        selfieToConfirm = selfieFile,
                        errorMessage = StringResource.ResId(
                            R.string.si_smart_selfie_processing_success_subtitle,
                        ),
                    )
                }
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error detecting faces")
            cleanupImageFiles()
            result = SmileIDResult.Error(exception)
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Error,
                    errorMessage = StringResource.ResId(R.string.si_processing_error_subtitle),
                )
            }
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private fun setCameraFacingMetadata(camSelector: CamSelector) {
        metadata.removeAll { it is Metadatum.SelfieImageOrigin }
        when (camSelector) {
            CamSelector.Front -> metadata.add(Metadatum.SelfieImageOrigin(FrontCamera))
            CamSelector.Back -> metadata.add(Metadatum.SelfieImageOrigin(BackCamera))
        }
    }

    private fun hasFaceRotatedEnough(face: Face): Boolean {
        val rotationXDelta = (face.headEulerAngleX - previousHeadRotationX).absoluteValue
        val rotationYDelta = (face.headEulerAngleY - previousHeadRotationY).absoluteValue
        val rotationZDelta = (face.headEulerAngleZ - previousHeadRotationZ).absoluteValue
        return rotationXDelta > FACE_ROTATION_THRESHOLD ||
            rotationYDelta > FACE_ROTATION_THRESHOLD ||
            rotationZDelta > FACE_ROTATION_THRESHOLD
    }

    private fun submitJob(selfieFile: File, livenessFiles: List<File>) {
        metadata.add(Metadatum.ActiveLivenessType(LivenessType.Smile))
        metadata.add(Metadatum.SelfieCaptureRetries(selfieCaptureRetries))
        try {
            DeviceOrientationMetadata.shared.storeDeviceMovement()
        } catch (_: UninitializedPropertyAccessException) {
            /*
            In case .shared isn't initialised it throws the above exception. Given that the
            device movement is only metadata we ignore it and take no action.
             */
        }

        if (skipApiSubmission) {
            result = SmileIDResult.Success(SmartSelfieResult(selfieFile, livenessFiles, null))
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
            return
        }
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }

        /*
         We add the network retries to the metadata here, because of the way we implement
         the biometric kyc and document verification which is calling the selfie component
         with skip api submission. If it is added before the skip api submission, it will be
         added to the metadata twice.
         */
        metadata.add(Metadatum.NetworkRetries(networkRetries))

        val proxy = fun(e: Throwable) {
            val didMoveToSubmitted = handleOfflineJobFailure(jobId, e)
            if (didMoveToSubmitted) {
                this.selfieFile = getFileByType(jobId, FileType.SELFIE)
                this.livenessFiles.apply {
                    clear()
                    addAll(getFilesByType(jobId, FileType.LIVENESS))
                }
            }
            if (SmileID.allowOfflineMode && isNetworkFailure(e)) {
                result = SmileIDResult.Success(
                    SmartSelfieResult(
                        selfieFile = selfieFile,
                        livenessFiles = livenessFiles,
                        apiResponse = null,
                    ),
                )
                _uiState.update {
                    it.copy(
                        processingState = ProcessingState.Success,
                        errorMessage = StringResource.ResId(R.string.si_offline_message),
                    )
                }
            } else {
                val errorMessage: StringResource = when {
                    isNetworkFailure(e) -> StringResource.ResId(R.string.si_no_internet)
                    e is SmileIDException -> StringResource.ResIdFromSmileIDException(e)
                    else -> StringResource.ResId(R.string.si_processing_error_subtitle)
                }
                result = SmileIDResult.Error(e)
                _uiState.update {
                    it.copy(
                        processingState = ProcessingState.Error,
                        errorMessage = errorMessage,
                    )
                }
            }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            if (SmileID.allowOfflineMode) {
                // For the moment, we continue to use the async API endpoints for offline mode
                val jobType = if (isEnroll) SmartSelfieEnrollment else SmartSelfieAuthentication
                val authRequest = AuthenticationRequest(
                    jobType = jobType,
                    enrollment = isEnroll,
                    userId = userId,
                    jobId = jobId,
                )
                createAuthenticationRequestFile(jobId, authRequest)
                createPrepUploadFile(
                    jobId,
                    PrepUploadRequest(
                        partnerParams = PartnerParams(
                            jobType = jobType,
                            jobId = jobId,
                            userId = userId,
                            extras = extraPartnerParams,
                        ),
                        allowNewEnroll = allowNewEnroll,
                        metadata = metadata,
                        timestamp = "",
                        signature = "",
                    ),
                )
            }

            val apiResponse = if (isEnroll) {
                SmileID.api.doSmartSelfieEnrollment(
                    selfieImage = selfieFile,
                    livenessImages = livenessFiles,
                    userId = userId,
                    partnerParams = extraPartnerParams,
                    allowNewEnroll = allowNewEnroll,
                    metadata = metadata.asNetworkRequest(),
                )
            } else {
                SmileID.api.doSmartSelfieAuthentication(
                    selfieImage = selfieFile,
                    livenessImages = livenessFiles,
                    userId = userId,
                    partnerParams = extraPartnerParams,
                    metadata = metadata.asNetworkRequest(),
                )
            }
            // Move files from unsubmitted to submitted directories
            val copySuccess = moveJobToSubmitted(jobId)
            val (selfieFileResult, livenessFilesResult) = if (copySuccess) {
                val selfieFileResult = getFileByType(jobId, FileType.SELFIE) ?: run {
                    Timber.w("Selfie file not found for job ID: $jobId")
                    throw IllegalStateException("Selfie file not found for job ID: $jobId")
                }
                val livenessFilesResult = getFilesByType(jobId, FileType.LIVENESS)
                selfieFileResult to livenessFilesResult
            } else {
                Timber.w("Failed to move job $jobId to complete")
                SmileIDCrashReporting.scopes.addBreadcrumb(
                    Breadcrumb().apply {
                        category = "Offline Mode"
                        message = "Failed to move job $jobId to complete"
                        level = SentryLevel.INFO
                    },
                )
                selfieFile to livenessFiles
            }

            networkRetries = 0
            metadata.removeAll { it is Metadatum.NetworkRetries }

            result = SmileIDResult.Success(
                SmartSelfieResult(
                    selfieFile = selfieFileResult,
                    livenessFiles = livenessFilesResult,
                    apiResponse = apiResponse,
                ),
            )
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Success,
                    errorMessage = StringResource.ResId(
                        R.string.si_smart_selfie_processing_success_subtitle,
                    ),
                )
            }
        }
    }

    fun onSelfieRejected() {
        _uiState.update {
            it.copy(
                processingState = null,
                selfieToConfirm = null,
                progress = 0f,
            )
        }
        cleanupImageFiles()
        result = null
        selfieCaptureRetries++
        shouldAnalyzeImages = true
        hasRecordedOrientationAtCaptureStart = false
        metadata.removeAll { it is Metadatum.DeviceOrientation }
    }

    fun onRetry() {
        // If selfie file is present, all captures were completed, so we're retrying a network issue
        if (selfieFile != null && livenessFiles.size == NUM_LIVENESS_IMAGES) {
            networkRetries++
            submitJob(selfieFile!!, livenessFiles)
        } else {
            cleanupImageFiles()
            metadata.removeAll { it is Metadatum.SelfieCaptureDuration }
            metadata.removeAll { it is Metadatum.ActiveLivenessType }
            metadata.removeAll { it is Metadatum.SelfieImageOrigin }
            selfieCaptureRetries++
            shouldAnalyzeImages = true
            _uiState.update {
                it.copy(processingState = null)
            }
        }
    }

    fun submitJob() {
        submitJob(selfieFile!!, livenessFiles)
    }

    fun onFinished(callback: SmileIDCallback<SmartSelfieResult>) {
        callback(result!!)
    }

    private fun cleanupImageFiles() {
        selfieFile?.delete()?.also { deleted ->
            if (!deleted) Timber.w("Failed to delete $selfieFile")
        }
        livenessFiles.removeAll { it.delete() }
        selfieFile = null
    }

    private fun isFaceStable(face: Rect): Boolean {
        val now = System.currentTimeMillis()

        // Remove entries older than 3 seconds
        val iterator = faceBoundingBoxQueue.iterator()
        while (iterator.hasNext()) {
            val (timestamp, _) = iterator.next()
            if (now - timestamp > FACE_STABILITY_WINDOW_MS) {
                iterator.remove()
            }
        }

        // Add new bounding box
        faceBoundingBoxQueue.addLast(now to Rect(face))

        if (faceBoundingBoxQueue.size < 5) return false

        // Allow some movement by checking average displacement from the mean center
        val centers = faceBoundingBoxQueue.map { (_, box) -> box.centerX() to box.centerY() }

        val avgX = centers.map { it.first }.average()
        val avgY = centers.map { it.second }.average()

        val maxDeviation = centers.maxOf {
            val dx = it.first - avgX
            val dy = it.second - avgY
            sqrt((dx * dx + dy * dy))
        }

        // Allow ~25 pixels of deviation as "stable enough"
        return maxDeviation < 25.0
    }
}
