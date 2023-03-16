package com.smileidentity.ui.viewmodel

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
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.JobStatusRequest
import com.smileidentity.networking.models.JobStatusResponse
import com.smileidentity.networking.models.JobType.SmartSelfieAuthentication
import com.smileidentity.networking.models.JobType.SmartSelfieEnrollment
import com.smileidentity.networking.models.PrepUploadRequest
import com.smileidentity.networking.models.UploadRequest
import com.smileidentity.ui.R
import com.smileidentity.ui.compose.ProcessingState
import com.smileidentity.ui.core.BitmapUtils
import com.smileidentity.ui.core.SmartSelfieResult
import com.smileidentity.ui.core.area
import com.smileidentity.ui.core.createLivenessFile
import com.smileidentity.ui.core.createSelfieFile
import com.smileidentity.ui.core.getExceptionHandler
import com.smileidentity.ui.core.postProcessImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

private const val INTRA_IMAGE_MIN_DELAY_MS = 350L
private const val NUM_LIVENESS_IMAGES = 7
private const val TOTAL_STEPS = NUM_LIVENESS_IMAGES + 1 // 7 B&W Liveness + 1 Color Selfie
private val LIVENESS_IMAGE_SIZE = Size(256, 256)
private val SELFIE_IMAGE_SIZE = Size(320, 320)
private const val NO_FACE_RESET_DELAY_MS = 3000L

data class SelfieUiState(
    val currentDirective: Directive = Directive.InitialInstruction,
    val progress: Float = 0f,
    val selfieToConfirm: File? = null,
    val processingState: ProcessingState? = null,
    val errorMessage: String? = null,
)

enum class Directive(@StringRes val displayText: Int) {
    InitialInstruction(R.string.si_smart_selfie_instructions),
    Capturing(R.string.si_smartselfie_directive_capturing),
    EnsureFaceInFrame(R.string.si_smartselfie_directive_unable_to_detect_face),
    MoveCloser(R.string.si_smartselfie_directive_face_too_far),
    MoveAway(R.string.si_smartselfie_directive_face_too_close),
    Smile(R.string.si_smartselfie_directive_smile),
}

class SelfieViewModel(private val isEnroll: Boolean, private val userId: String) : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmartSelfieResult? = null

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
        // TODO: Test if we get better performance on low-end devices if this is disabled until we
        //  actually need to detect the smile for the last image
        setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
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

            // Check that the face is close enough to the camera
            val minFaceAreaThreshold = 0.15
            if (faceFillRatio < minFaceAreaThreshold) {
                _uiState.update { it.copy(currentDirective = Directive.MoveCloser) }
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            val maxFaceAreaThreshold = 0.25
            if (faceFillRatio > maxFaceAreaThreshold) {
                _uiState.update { it.copy(currentDirective = Directive.MoveAway) }
                return@addOnSuccessListener
            }

            // Ensure that the last image contains a smile
            val smileThreshold = 0.8
            val isSmiling = (largestFace.smilingProbability ?: 0f) > smileThreshold
            if (livenessFiles.size == NUM_LIVENESS_IMAGES && !isSmiling) {
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

            // TODO: CameraX 1.3.0-alpha04 added built0n API to convert ImageProxy to Bitmap
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
        }.addOnFailureListener {
            Timber.e(it, "Error detecting faces")
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private fun hasFaceRotatedEnough(face: Face): Boolean {
        val rotationThreshold = 1.5f
        val rotationXDelta = (face.headEulerAngleX - previousHeadRotationX).absoluteValue
        val rotationYDelta = (face.headEulerAngleY - previousHeadRotationY).absoluteValue
        val rotationZDelta = (face.headEulerAngleZ - previousHeadRotationZ).absoluteValue
        return rotationXDelta > rotationThreshold ||
            rotationYDelta > rotationThreshold ||
            rotationZDelta > rotationThreshold
    }

    private fun submitJob(selfieFile: File, livenessFiles: List<File>) {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
        val proxy = { e: Throwable ->
            result = SmartSelfieResult.Error(e)
            _uiState.update {
                it.copy(processingState = ProcessingState.Error, errorMessage = e.message)
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val jobType = if (isEnroll) SmartSelfieEnrollment else SmartSelfieAuthentication
            val authRequest = AuthenticationRequest(
                jobType = jobType,
                enrollment = isEnroll,
                userId = userId,
            )

            val authResponse = SmileIdentity.api.authenticate(authRequest)

            val prepUploadRequest = PrepUploadRequest(
                callbackUrl = "",
                partnerParams = authResponse.partnerParams,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val prepUploadResponse = SmileIdentity.api.prepUpload(prepUploadRequest)
            val livenessImagesInfo = livenessFiles.map { it.asLivenessImage() }
            val selfieImageInfo = selfieFile.asSelfieImage()
            val uploadRequest = UploadRequest(livenessImagesInfo + selfieImageInfo)
            SmileIdentity.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
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
                jobStatusResponse = SmileIdentity.api.getJobStatus(jobStatusRequest)
                Timber.v("Job Status Response: $jobStatusResponse")
                if (jobStatusResponse.jobComplete) {
                    break
                }
            }
            result = SmartSelfieResult.Success(selfieFile, livenessFiles, jobStatusResponse)
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

    fun submitJob() {
        submitJob(selfieFile!!, livenessFiles)
    }

    fun onFinished(callback: SmartSelfieResult.Callback) {
        callback.onResult(result!!)
    }
}
