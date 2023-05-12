package com.smileidentity.viewmodel

import android.util.Size
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.createDocumentFile
import com.smileidentity.postProcessImage
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val ID_DOCUMENT_IMAGE_SIZE = Size(320, 320)
private val PASSPORT_DOCUMENT_IMAGE_SIZE = Size(320, 320)

data class DocumentUiState(
    val documentImageToConfirm: File? = null,
    @StringRes val errorMessage: Int? = null,
)

class DocumentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState = _uiState.asStateFlow()

    internal fun takeButtonCaptureDocument(
        cameraState: CameraState,
    ) {
        viewModelScope.launch {
            try {
                val documentFile = captureDocument(cameraState)
                Timber.v("Capturing document image to $documentFile")
                _uiState.update { it.copy(documentImageToConfirm = documentFile) }
            } catch (e: Exception) {
                Timber.e("Error capturing document", e)
                _uiState.update { it.copy(errorMessage = R.string.si_doc_v_capture_error_subtitle) }
            }
        }
    }

    /**
     * Captures a document image using the given [cameraState] and returns the processed image as a [Bitmap].
     * If an error occurs during capture or processing, the coroutine will be resumed with an exception.
     * The [documentFile] variable will be updated with the captured image file, and the UI state will be updated accordingly.
     */
    private suspend fun captureDocument(cameraState: CameraState) = suspendCoroutine {
        val documentFile = createDocumentFile()
        cameraState.takePicture(documentFile) { result ->
            when (result) {
                is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
                is ImageCaptureResult.Success -> it.resume(
                    postProcessImage(
                        file = documentFile,
                        saveAsGrayscale = false,
                        compressionQuality = 80,
                        desiredOutputSize = ID_DOCUMENT_IMAGE_SIZE,
                    ),
                )
            }
        }
    }
}
