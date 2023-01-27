package com.smileidentity.ui.viewmodel

import android.annotation.SuppressLint
import android.util.Size
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.JobStatusRequest
import com.smileidentity.networking.models.JobStatusResponse
import com.smileidentity.networking.models.JobType
import com.smileidentity.networking.models.PrepUploadRequest
import com.smileidentity.networking.models.UploadRequest
import com.smileidentity.ui.R
import com.smileidentity.ui.core.BitmapUtils
import com.smileidentity.ui.core.SmartSelfieResult
import com.smileidentity.ui.core.SmartSelfieResult.Success
import com.smileidentity.ui.core.area
import com.smileidentity.ui.core.createLivenessFile
import com.smileidentity.ui.core.createSelfieFile
import com.smileidentity.ui.core.getExceptionHandler
import com.smileidentity.ui.core.postProcessImageBitmap
import com.smileidentity.ui.core.postProcessImageFile
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

data class SelfieUiState(
    @StringRes val currentDirective: Int = R.string.si_smartselfie_instructions,
    val progress: Float = 0f,
    val isCapturing: Boolean = false,
    val isWaitingForResult: Boolean = false,
)

class SelfieViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieUiState())
    val uiState: StateFlow<SelfieUiState> = _uiState.asStateFlow()

    private val intraImageMinDelayMs = 350L
    private val numLivenessImages = 7
    private val totalSteps = numLivenessImages + 1 // 7 B&W Liveness + 1 Color Selfie
    private val livenessImageSize = Size(256, 256)
    private val selfieImageSize = Size(320, 320)
    private val livenessFiles = mutableListOf<File>()
    private var lastAutoCaptureTimeMs = 0L

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

    fun takeButtonInitiatedPictures(
        cameraState: CameraState,
        callback: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
    ) {
        shouldAnalyzeImages = false
        _uiState.update {
            it.copy(
                isCapturing = true,
                currentDirective = R.string.si_smartselfie_directive_capturing,
            )
        }

        val proxy = { e: Throwable -> callback.onResult(SmartSelfieResult.Error(e)) }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            // Resume from where we left off, if we already have already taken some images
            val startingImageNum = livenessFiles.size + 1
            for (stepNum in startingImageNum..numLivenessImages) {
                delay(intraImageMinDelayMs)
                val livenessFile = captureLivenessImage(cameraState)
                livenessFiles.add(livenessFile)
                _uiState.update { it.copy(progress = stepNum / totalSteps.toFloat()) }
            }
            delay(intraImageMinDelayMs)
            val selfieFile = captureSelfieImage(cameraState)
            _uiState.update { it.copy(progress = 1f) }
            val jobStatusResponse = submit(selfieFile, livenessFiles)
            callback.onResult(Success(selfieFile, livenessFiles, jobStatusResponse))
        }
    }

    private suspend fun captureSelfieImage(cameraState: CameraState) = suspendCoroutine {
        val file = createSelfieFile()
        cameraState.takePicture(file) { result ->
            when (result) {
                is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
                is ImageCaptureResult.Success -> it.resume(
                    postProcessImageFile(
                        file,
                        saveAsGrayscale = false,
                        compressionQuality = 80,
                        maxOutputSize = selfieImageSize,
                    ),
                )
            }
        }
    }

    private suspend fun captureLivenessImage(cameraState: CameraState) = suspendCoroutine {
        val file = createLivenessFile()
        cameraState.takePicture(file) { result ->
            when (result) {
                is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
                is ImageCaptureResult.Success -> it.resume(
                    postProcessImageFile(
                        file,
                        saveAsGrayscale = true,
                        compressionQuality = 80,
                        maxOutputSize = livenessImageSize,
                    ),
                )
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun analyzeImage(imageProxy: ImageProxy, callback: SmartSelfieResult.Callback) {
        val image = imageProxy.image
        val elapsedTime = System.currentTimeMillis() - lastAutoCaptureTimeMs
        if (!shouldAnalyzeImages || elapsedTime < intraImageMinDelayMs || image == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            Timber.d("Detected Faces: $faces")
            if (faces.isEmpty()) {
                _uiState.update {
                    it.copy(currentDirective = R.string.si_smartselfie_directive_unable_to_detect_face)
                }
                return@addOnSuccessListener
            }

            // Pick the largest face
            val largestFace = faces.maxBy { it.boundingBox.area }
            val faceFillRatio = (largestFace.boundingBox.area / inputImage.area.toFloat())

            // Check that the face is close enough to the camera
            val minFaceAreaThreshold = 0.25
            if (faceFillRatio < minFaceAreaThreshold) {
                _uiState.update {
                    it.copy(currentDirective = R.string.si_smartselfie_directive_face_too_far)
                }
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            val maxFaceAreaThreshold = 0.50
            if (faceFillRatio > maxFaceAreaThreshold) {
                _uiState.update {
                    it.copy(currentDirective = R.string.si_smartselfie_directive_face_too_close)
                }
                return@addOnSuccessListener
            }

            // Ensure that the last image contains a smile
            val smileThreshold = 0.8
            val isSmiling = (largestFace.smilingProbability ?: 0f) > smileThreshold
            if (livenessFiles.size == numLivenessImages && !isSmiling) {
                _uiState.update {
                    it.copy(currentDirective = R.string.si_smartselfie_directive_smile)
                }
                return@addOnSuccessListener
            }

            _uiState.update {
                it.copy(currentDirective = R.string.si_smartselfie_directive_capturing)
            }

            BitmapUtils.getBitmap(imageProxy)?.let { bitmap ->
                // All conditions satisfied, capture the image
                lastAutoCaptureTimeMs = System.currentTimeMillis()
                if (livenessFiles.size < numLivenessImages) {
                    Timber.v("Capturing liveness image")
                    val livenessFile = createLivenessFile()
                    postProcessImageBitmap(
                        bitmap = bitmap,
                        file = livenessFile,
                        saveAsGrayscale = true,
                        compressionQuality = 80,
                        maxOutputSize = livenessImageSize,
                    )
                    livenessFiles.add(livenessFile)
                    _uiState.update {
                        it.copy(progress = livenessFiles.size / totalSteps.toFloat())
                    }
                } else {
                    Timber.v("Capturing selfie image")
                    val selfieFile = createSelfieFile()
                    postProcessImageBitmap(
                        bitmap = bitmap,
                        file = selfieFile,
                        saveAsGrayscale = false,
                        compressionQuality = 80,
                        maxOutputSize = selfieImageSize,
                    )
                    _uiState.update {
                        it.copy(progress = 1f)
                    }
                    shouldAnalyzeImages = false
                    val proxy = { e: Throwable -> callback.onResult(SmartSelfieResult.Error(e)) }
                    viewModelScope.launch(getExceptionHandler(proxy)) {
                        val jobStatusResponse = submit(selfieFile, livenessFiles)
                        callback.onResult(Success(selfieFile, livenessFiles, jobStatusResponse))
                    }
                }
            }
        }.addOnFailureListener {
            Timber.e(it, "Error detecting faces")
        }.addOnCompleteListener { faces ->
            Timber.d("Complete: $faces")
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private suspend fun submit(selfieFile: File, livenessFiles: List<File>): JobStatusResponse {
        _uiState.update { it.copy(isWaitingForResult = true) }

        val authRequest = AuthenticationRequest(
            jobType = JobType.SmartSelfieEnrollment,
            enrollment = true,
        )
        val authResponse = SmileIdentity.api.authenticate(authRequest)

        val prepUploadRequest = PrepUploadRequest(
            callbackUrl = "",
            partnerParams = authResponse.partnerParams,
            signature = authResponse.signature,
            timestamp = authResponse.timestamp,
        )
        val prepUploadResponse = SmileIdentity.api.prepUpload(prepUploadRequest)
        Timber.d("Prep Upload Response: $prepUploadResponse")
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
        return jobStatusResponse
    }
}
