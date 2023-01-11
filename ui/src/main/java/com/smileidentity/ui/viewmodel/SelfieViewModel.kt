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
import com.smileidentity.ui.R
import com.smileidentity.ui.core.BitmapUtils
import com.smileidentity.ui.core.SelfieCaptureResult
import com.smileidentity.ui.core.SelfieCaptureResultCallback
import com.smileidentity.ui.core.area
import com.smileidentity.ui.core.createLivenessFile
import com.smileidentity.ui.core.createSelfieFile
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

data class SelfieUiState(
    @StringRes val currentDirective: Int = R.string.si_selfie_capture_directive_smile,
    val progress: Float = 0f,
)

val SelfieUiState.isCapturing: Boolean
    get() = currentDirective == R.string.si_selfie_capture_directive_capturing

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
    internal var isAutoCaptureComplete = false

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
        callback: SelfieCaptureResultCallback = SelfieCaptureResultCallback {},
    ) {
        _uiState.update {
            it.copy(currentDirective = R.string.si_selfie_capture_directive_capturing)
        }

        viewModelScope.launch {
            try {
                // Resume from where we left off, if we already have already taken some images
                val startingImageNum = livenessFiles.size + 1
                for (stepNum in startingImageNum..numLivenessImages) {
                    delay(intraImageMinDelayMs)
                    val livenessFile = captureLivenessImage(cameraState)
                    livenessFiles.add(livenessFile)
                    _uiState.update { it.copy(progress = stepNum / totalSteps.toFloat()) }
                }
                val selfieFile = captureSelfieImage(cameraState)
                _uiState.update { it.copy(progress = 1f) }
                callback.onResult(SelfieCaptureResult.Success(selfieFile, livenessFiles))
            } catch (e: Exception) {
                Timber.e("Error capturing images", e)
                _uiState.update { it.copy(progress = 0f) }
            }
            _uiState.update {
                it.copy(currentDirective = R.string.si_selfie_capture_directive_smile)
            }
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
                        desiredOutputSize = selfieImageSize,
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
                        desiredOutputSize = livenessImageSize,
                    ),
                )
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun analyzeImage(
        proxy: ImageProxy,
        callback: SelfieCaptureResultCallback = SelfieCaptureResultCallback {},
    ) {
        val elapsedTime = System.currentTimeMillis() - lastAutoCaptureTimeMs
        if (isAutoCaptureComplete || elapsedTime < intraImageMinDelayMs) {
            proxy.close()
            return
        }

        val image = proxy.image
        if (image == null) {
            proxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, proxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            Timber.d("Detected Faces: $faces")
            if (faces.isEmpty()) {
                _uiState.update {
                    it.copy(currentDirective = R.string.si_selfie_capture_directive_unable_to_detect_face)
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
                    it.copy(currentDirective = R.string.si_selfie_capture_directive_face_too_far)
                }
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            val maxFaceAreaThreshold = 0.50
            if (faceFillRatio > maxFaceAreaThreshold) {
                _uiState.update {
                    it.copy(currentDirective = R.string.si_selfie_capture_directive_face_too_close)
                }
                return@addOnSuccessListener
            }

            // Ensure that the last image contains a smile
            val smileThreshold = 0.8
            val isSmiling = (largestFace.smilingProbability ?: 0f) > smileThreshold
            if (livenessFiles.size == numLivenessImages && !isSmiling) {
                _uiState.update {
                    it.copy(currentDirective = R.string.si_selfie_capture_directive_smile)
                }
                return@addOnSuccessListener
            }

            _uiState.update {
                it.copy(currentDirective = R.string.si_selfie_capture_directive_capturing)
            }

            BitmapUtils.getBitmap(proxy)?.let { bitmap ->
                // All conditions satisfied, capture the image
                lastAutoCaptureTimeMs = System.currentTimeMillis()
                if (livenessFiles.size < numLivenessImages) {
                    Timber.d("Capturing liveness image")
                    val file = createLivenessFile()
                    postProcessImageBitmap(
                        bitmap = bitmap,
                        file = file,
                        saveAsGrayscale = true,
                        compressionQuality = 80,
                        desiredOutputSize = livenessImageSize,
                    )
                    livenessFiles.add(file)
                    _uiState.update {
                        it.copy(progress = livenessFiles.size / totalSteps.toFloat())
                    }
                } else {
                    Timber.d("Capturing selfie image")
                    val file = createSelfieFile()
                    postProcessImageBitmap(
                        bitmap = bitmap,
                        file = file,
                        saveAsGrayscale = false,
                        compressionQuality = 80,
                        desiredOutputSize = selfieImageSize,
                    )
                    _uiState.update {
                        it.copy(progress = 1f)
                    }
                    callback.onResult(SelfieCaptureResult.Success(file, livenessFiles))
                    isAutoCaptureComplete = true
                }
            }
        }.addOnFailureListener {
            Timber.e(it, "Error detecting faces")
        }.addOnCompleteListener { faces ->
            Timber.d("Complete: $faces")
            // Closing the proxy allows the next image to be delivered to the analyzer
            proxy.close()
        }
    }
}
