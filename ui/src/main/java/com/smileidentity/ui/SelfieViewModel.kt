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
import timber.log.Timber
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

    fun takePicture(cameraState: CameraState) {
        // Save to temporary file, which does not require any storage permissions. It will be saved
        // to the app's cache directory, which is cleared when the app is uninstalled. Images will
        // be saved in the format "si_selfie_<random number>.jpg"
        val file = File.createTempFile("si_selfie_", ".jpg")
        cameraState.takePicture(file) {
            if (it is ImageCaptureResult.Success) {
                Timber.d("Image captured successfully: $it, saved to file: $file")
            } else if (it is ImageCaptureResult.Error) {
                Timber.e(it.throwable, "Image capture error: $it")
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
            it.copy(
                currentDirective = directives.random(),
                progress = (((it.progress * 100) + 1) % 100) / 100
            )
        }
    }
}
