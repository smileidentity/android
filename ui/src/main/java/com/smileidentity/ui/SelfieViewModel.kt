package com.smileidentity.ui

import androidx.annotation.StringRes
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

data class SelfieUiState(
    @StringRes val currentDirective: Int,
    val allowAgentMode: Boolean = true,
    val progress: Float = 0f,
)

class SelfieViewModel : ViewModel() {
    private val _uiState =
        MutableStateFlow(SelfieUiState(R.string.si_selfie_capture_directive_smile))
    val uiState: StateFlow<SelfieUiState> = _uiState.asStateFlow()
    var count = 0

    fun takePicture(
        cameraState: CameraState,
        callback: SelfieCaptureResultCallback = SelfieCaptureResultCallback {}
    ) {
        // Save to temporary file, which does not require any storage permissions. It will be saved
        // to the app's cache directory, which is cleared when the app is uninstalled. Images will
        // be saved in the format "si_selfie_<random number>.jpg"
        val file = File.createTempFile("si_selfie_", ".jpg")
        cameraState.takePicture(file) {
            when (it) {
                is ImageCaptureResult.Success ->
                    callback.onResult(SelfieCaptureResult.Success(file.absolutePath))
                is ImageCaptureResult.Error ->
                    callback.onResult(SelfieCaptureResult.Error(it.throwable))
            }
        }

        // Deletes file when the *VM* is exited (*not* when the app is closed)
        file.deleteOnExit()
    }

    fun analyzeImage(proxy: ImageProxy) {
        proxy.close()
        val directives = setOf(
            R.string.si_selfie_capture_directive_smile,
            R.string.si_selfie_capture_directive_capturing,
            R.string.si_selfie_capture_directive_face_too_far,
            R.string.si_selfie_capture_directive_unable_to_detect_face
        )
        _uiState.update {
            val newDirective = if ((count % 25) == 0) directives.random() else it.currentDirective
            val newProgress = (((it.progress * 100) + 1) % 100) / 100
            it.copy(currentDirective = newDirective, progress = newProgress)
        }
        count += 1
    }
}
