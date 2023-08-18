package com.smileidentity.viewmodel.document

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.util.createDocumentFile
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.time.Duration.Companion.seconds

data class DocumentCaptureUiState(
    val acknowledgedInstructions: Boolean = false,
    val areEdgesDetected: Boolean = false,
    val showManualCaptureButton: Boolean = false,
    val documentImageToConfirm: File? = null,
    @StringRes val errorMessage: Int? = null,
)

class DocumentCaptureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentCaptureUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Show manual capture after 10 seconds, using coroutine
        viewModelScope.launch {
            delay(10.seconds)
            allowManualCapture()
        }
    }

    fun onInstructionsAcknowledged() {
        _uiState.update { it.copy(acknowledgedInstructions = true) }
    }

    /**
     * Called when auto capture has timed out or if capability is not supported
     */
    private fun allowManualCapture() {
        _uiState.update { it.copy(showManualCaptureButton = true) }
    }

    fun onPhotoSelectedFromGallery(selectedPhoto: File?) {
        if (selectedPhoto == null) {
            Timber.e("selectedPhoto is null")
            _uiState.update {
                it.copy(errorMessage = R.string.si_doc_v_capture_error_subtitle)
            }
        } else {
            _uiState.update {
                it.copy(acknowledgedInstructions = true, documentImageToConfirm = selectedPhoto)
            }
        }
    }

    /**
     * To be called when auto capture determines the image quality is sufficient or when the user
     * taps the manual capture button
     */
    fun captureDocument(cameraState: CameraState) {
        // todo: lock this method to prevent multiple calls
        val documentFile = createDocumentFile()
        cameraState.takePicture(documentFile) { result ->
            when (result) {
                is ImageCaptureResult.Success -> {
                    _uiState.update { it.copy(documentImageToConfirm = documentFile) }
                }

                is ImageCaptureResult.Error -> {
                    Timber.e("Error capturing document", result.throwable)
                    _uiState.update {
                        it.copy(errorMessage = R.string.si_doc_v_capture_error_subtitle)
                    }
                }
            }
        }
    }

    fun onRetry() {
        // It is safe to delete the file here, EVEN THOUGH it may have been selected from gallery
        // because the URI we get back from the PhotoPicker does not grant us write access
        uiState.value.documentImageToConfirm?.delete()
        _uiState.update {
            it.copy(
                errorMessage = null,
                documentImageToConfirm = null,
                acknowledgedInstructions = false,
            )
        }
    }
}
