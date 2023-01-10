package com.smileidentity.ui.viewmodel

import android.util.Size
import androidx.annotation.StringRes
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.ui.R
import com.smileidentity.ui.core.SelfieCaptureResult
import com.smileidentity.ui.core.SelfieCaptureResultCallback
import com.smileidentity.ui.core.createLivenessFile
import com.smileidentity.ui.core.createSelfieFile
import com.smileidentity.ui.core.postProcessImage
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
    val isCapturing: Boolean = false,
)

class SelfieViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieUiState())
    val uiState: StateFlow<SelfieUiState> = _uiState.asStateFlow()
    var count = 0 // TODO: Remove this count, it is for demo purposes only

    fun takePicture(
        cameraState: CameraState,
        callback: SelfieCaptureResultCallback = SelfieCaptureResultCallback {},
    ) {
        _uiState.update { it.copy(isCapturing = true) }

        // Take 1 color photo
        // Take 7 B&W photos
        viewModelScope.launch {
            val numLivenessImages = 7
            val totalSteps = numLivenessImages + 1
            try {
                val selfieFile = captureSelfieImage(cameraState)
                _uiState.update { it.copy(progress = 1f / totalSteps) }
                val livenessFiles = mutableListOf<File>()
                for (stepNum in 2..totalSteps) {
                    delay(500)
                    val livenessFile = captureLivenessImage(cameraState)
                    livenessFiles.add(livenessFile)
                    _uiState.update { it.copy(progress = stepNum / totalSteps.toFloat()) }
                }
                callback.onResult(SelfieCaptureResult.Success(selfieFile, livenessFiles))
            } catch (e: Exception) {
                Timber.e("Error capturing images", e)
                _uiState.update { it.copy(progress = 0f) }
            }
            _uiState.update { it.copy(isCapturing = false) }
        }
    }

    private suspend fun captureSelfieImage(cameraState: CameraState) = suspendCoroutine {
        val file = createSelfieFile()
        cameraState.takePicture(file) { result ->
            when (result) {
                is ImageCaptureResult.Success -> it.resume(
                    postProcessImage(
                        file,
                        saveAsGrayscale = false,
                        compressionQuality = 80,
                        desiredOutputSize = Size(320, 320),
                    ),
                )
                is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
            }
        }
    }

    private suspend fun captureLivenessImage(cameraState: CameraState) = suspendCoroutine {
        val file = createLivenessFile()
        cameraState.takePicture(file) { result ->
            when (result) {
                is ImageCaptureResult.Success -> it.resume(
                    postProcessImage(
                        file,
                        saveAsGrayscale = true,
                        compressionQuality = 80,
                        desiredOutputSize = Size(256, 256),
                    ),
                )
                is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
            }
        }
    }

    fun analyzeImage(proxy: ImageProxy) {
        // TODO: Implement image analysis
        proxy.close()
        val directives = setOf(
            R.string.si_selfie_capture_directive_smile,
            R.string.si_selfie_capture_directive_capturing,
            R.string.si_selfie_capture_directive_face_too_far,
            R.string.si_selfie_capture_directive_unable_to_detect_face,
        )
        _uiState.update {
            // TODO: Remove. For demo purposes only
            val newDirective = if ((count % 25) == 0) directives.random() else it.currentDirective
            it.copy(currentDirective = newDirective)
        }
        count += 1
    }
}
