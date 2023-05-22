package com.smileidentity.viewmodel

import android.annotation.SuppressLint
import android.util.Size
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.BitmapUtils
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.area
import com.smileidentity.compose.ProcessingState
import com.smileidentity.createLivenessFile
import com.smileidentity.createSelfieFile
import com.smileidentity.getExceptionHandler
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobStatusResponse
import com.smileidentity.models.JobType.SmartSelfieAuthentication
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.postProcessImageBitmap
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

private const val INTRA_IMAGE_MIN_DELAY_MS = 350
private const val NUM_LIVENESS_IMAGES = 7
private const val TOTAL_STEPS = NUM_LIVENESS_IMAGES + 1 // 7 B&W Liveness + 1 Color Selfie
private val LIVENESS_IMAGE_SIZE = Size(256, 256)
private val SELFIE_IMAGE_SIZE = Size(320, 320)
private const val NO_FACE_RESET_DELAY_MS = 3000
private const val FACE_ROTATION_THRESHOLD = 0.75f
private const val MIN_FACE_AREA_THRESHOLD = 0.15f
const val MAX_FACE_AREA_THRESHOLD = 0.25f
private const val SMILE_THRESHOLD = 0.8f

data class SelfieUiState(
    val currentDirective: Directive = Directive.InitialInstruction,
    val progress: Float = 0f,
    val selfieToConfirm: File? = null,
    val processingState: ProcessingState? = null,
    @StringRes val errorMessage: Int? = null,
)

enum class Directive(@StringRes val displayText: Int) {
    InitialInstruction(R.string.si_smart_selfie_instructions),
    Capturing(R.string.si_smart_selfie_directive_capturing),
    EnsureFaceInFrame(R.string.si_smart_selfie_directive_unable_to_detect_face),
    MoveCloser(R.string.si_smart_selfie_directive_face_too_far),
    MoveAway(R.string.si_smart_selfie_directive_face_too_close),
    Smile(R.string.si_smart_selfie_directive_smile),
}

class SelfieViewModel(
    private val isEnroll: Boolean,
    private val userId: String,
    private val jobId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<SmartSelfieResult>? = null

    private val livenessFiles = mutableListOf<File>()
    private var selfieFile: File? = null
    private var lastAutoCaptureTimeMs = 0L
    private var previousHeadRotationX = Float.POSITIVE_INFINITY
    private var previousHeadRotationY = Float.POSITIVE_INFINITY
    private var previousHeadRotationZ = Float.POSITIVE_INFINITY

    @VisibleForTesting
    internal var shouldAnalyzeImages = true

    private val faceDetectorOptions = FaceDetectorOptions.Builder().apply {
        setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        // TODO: setMinFaceSize(MIN_FACE_AREA_THRESHOLD)
    }.build()
    private val faceDetector by lazy { FaceDetection.getClient(faceDetectorOptions) }

    @SuppressLint("UnsafeOptInUsageError")
    internal fun analyzeImage(imageProxy: ImageProxy) {
        val image = imageProxy.image
        val elapsedTimeMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        if (!shouldAnalyzeImages || elapsedTimeMs < INTRA_IMAGE_MIN_DELAY_MS || image == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            if (faces.isEmpty()) {
                _uiState.update { it.copy(currentDirective = Directive.EnsureFaceInFrame) }
                // If no faces are detected for a while, reset the state
                if (elapsedTimeMs > NO_FACE_RESET_DELAY_MS) {
                    _uiState.update {
                        it.copy(
                            progress = 0f,
                            selfieToConfirm = null,
                            processingState = null,
                            errorMessage = null,
                        )
                    }
                    livenessFiles.removeAll { it.delete() }
                    selfieFile?.delete()
                    selfieFile = null
                }
                return@addOnSuccessListener
            }

            // Pick the largest face
            val largestFace = faces.maxBy { it.boundingBox.area }
            val faceFillRatio = (largestFace.boundingBox.area / inputImage.area.toFloat())

            // Check that the corners of the face bounding box are within the inputImage
            val faceCornersInImage = largestFace.boundingBox.left >= 0 &&
                largestFace.boundingBox.right <= inputImage.width &&
                largestFace.boundingBox.top >= 0 &&
                largestFace.boundingBox.bottom <= inputImage.height
            if (!faceCornersInImage) {
                _uiState.update { it.copy(currentDirective = Directive.EnsureFaceInFrame) }
                return@addOnSuccessListener
            }

            // Check that the face is close enough to the camera
            if (faceFillRatio < MIN_FACE_AREA_THRESHOLD) {
                _uiState.update { it.copy(currentDirective = Directive.MoveCloser) }
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            if (faceFillRatio > MAX_FACE_AREA_THRESHOLD) {
                _uiState.update { it.copy(currentDirective = Directive.MoveAway) }
                return@addOnSuccessListener
            }

            // Ask the user to start smiling partway through liveness images
            val isSmiling = (largestFace.smilingProbability ?: 0f) > SMILE_THRESHOLD
            if (livenessFiles.size > NUM_LIVENESS_IMAGES / 2 && !isSmiling) {
                _uiState.update { it.copy(currentDirective = Directive.Smile) }
                return@addOnSuccessListener
            }

            _uiState.update { it.copy(currentDirective = Directive.Capturing) }

            // Perform the rotation checks *after* changing directive to Capturing -- we don't want
            // to explicitly tell the user to move their head
            if (!hasFaceRotatedEnough(largestFace)) {
                Timber.v("Not enough face rotation between captures. Waiting...")
                return@addOnSuccessListener
            }
            previousHeadRotationX = largestFace.headEulerAngleX
            previousHeadRotationY = largestFace.headEulerAngleY
            previousHeadRotationZ = largestFace.headEulerAngleZ

            // TODO: CameraX 1.3.0-alpha04 adds built-in API to convert ImageProxy to Bitmap.
            //  Incorporate once stable
            BitmapUtils.getBitmap(imageProxy)?.let { bitmap ->
                // All conditions satisfied, capture the image
                lastAutoCaptureTimeMs = System.currentTimeMillis()
                if (livenessFiles.size < NUM_LIVENESS_IMAGES) {
                    Timber.v("Capturing liveness image")
                    val livenessFile = createLivenessFile()
                    postProcessImageBitmap(
                        bitmap = bitmap,
                        file = livenessFile,
                        saveAsGrayscale = true,
                        compressionQuality = 80,
                        maxOutputSize = LIVENESS_IMAGE_SIZE,
                    )
                    livenessFiles.add(livenessFile)
                    _uiState.update {
                        it.copy(progress = livenessFiles.size / TOTAL_STEPS.toFloat())
                    }
                } else {
                    selfieFile = createSelfieFile()
                    Timber.v("Capturing selfie image to $selfieFile")
                    postProcessImageBitmap(
                        bitmap = bitmap,
                        file = selfieFile!!,
                        saveAsGrayscale = false,
                        compressionQuality = 80,
                        maxOutputSize = SELFIE_IMAGE_SIZE,
                    )
                    shouldAnalyzeImages = false
                    _uiState.update { it.copy(progress = 1f, selfieToConfirm = selfieFile) }
                }
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error detecting faces")
            result = SmileIDResult.Error(exception)
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Error,
                    errorMessage = R.string.si_smart_selfie_processing_error_subtitle,
                )
            }
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
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
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
        val proxy = { e: Throwable ->
            result = SmileIDResult.Error(e)
            _uiState.update {
                it.copy(
                    processingState = ProcessingState.Error,
                    errorMessage = R.string.si_smart_selfie_processing_error_subtitle,
                )
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val jobType = if (isEnroll) SmartSelfieEnrollment else SmartSelfieAuthentication
            val authRequest = AuthenticationRequest(
                jobType = jobType,
                enrollment = isEnroll,
                userId = userId,
                jobId = jobId,
            )

            val authResponse = SmileID.api.authenticate(authRequest)

            val prepUploadRequest = PrepUploadRequest(
                callbackUrl = "",
                partnerParams = authResponse.partnerParams,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val prepUploadResponse = SmileID.api.prepUpload(prepUploadRequest)
            val livenessImagesInfo = livenessFiles.map { it.asLivenessImage() }
            val selfieImageInfo = selfieFile.asSelfieImage()
            val uploadRequest = UploadRequest(livenessImagesInfo + selfieImageInfo)
            SmileID.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
            Timber.d("Upload finished")
            val jobStatusRequest = JobStatusRequest(
                jobId = authResponse.partnerParams.jobId,
                userId = authResponse.partnerParams.userId,
                includeImageLinks = false,
                includeHistory = false,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )

            lateinit var jobStatusResponse: JobStatusResponse
            val jobStatusPollDelay = 1.seconds
            for (i in 1..10) {
                Timber.v("Job Status poll attempt #$i in $jobStatusPollDelay")
                delay(jobStatusPollDelay)
                jobStatusResponse = SmileID.api.getJobStatus(jobStatusRequest)
                Timber.v("Job Status Response: $jobStatusResponse")
                if (jobStatusResponse.jobComplete) {
                    break
                }
            }
            result = SmileIDResult.Success(
                SmartSelfieResult(
                    selfieFile,
                    livenessFiles,
                    jobStatusResponse,
                ),
            )
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onSelfieRejected() {
        _uiState.update {
            it.copy(
                processingState = null,
                selfieToConfirm = null,
                progress = (TOTAL_STEPS - 1) / TOTAL_STEPS.toFloat(),
            )
        }
        selfieFile?.delete()?.also { deleted ->
            if (!deleted) Timber.w("Failed to delete $selfieFile")
        }
        selfieFile = null
        result = null
        shouldAnalyzeImages = true
    }

    fun onRetry() {
        // If selfie file is present, all captures were completed, so we're retrying a network issue
        if (selfieFile != null) {
            submitJob(selfieFile!!, livenessFiles)
        } else {
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
}
