package com.smileidentity.viewmodel

import android.util.Size
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.createDocumentFile
import com.smileidentity.models.Document
import com.smileidentity.postProcessImage
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDResult
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

data class DocumentUiState(
    val frontDocumentImageToConfirm: File? = null,
    val backDocumentImageToConfirm: File? = null,
    @StringRes val errorMessage: Int? = null,
)

class DocumentViewModel(
    private val userId: String,
    private val jobId: String,
    private val enforcedIdType: Document? = null,
    private val idAspectRatio: Float? = enforcedIdType?.aspectRatio,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState = _uiState.asStateFlow()
    var result: SmileIDResult<DocumentVerificationResult>? = null
    private var documentFile: File? = null

    internal fun takeButtonCaptureDocument(
        cameraState: CameraState,
        isBackSide: Boolean,
    ) {
        viewModelScope.launch {
            try {
                val documentFile = captureDocument(cameraState)
                Timber.v("Capturing document image to $documentFile")
                _uiState.update { if (isBackSide) it.copy(backDocumentImageToConfirm = documentFile) else it.copy(frontDocumentImageToConfirm = documentFile) }
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
        documentFile = createDocumentFile()
        cameraState.takePicture(documentFile!!) { result ->
            when (result) {
                is ImageCaptureResult.Error -> it.resumeWithException(result.throwable)
                is ImageCaptureResult.Success -> it.resume(
                    postProcessImage(
                        file = documentFile!!,
                        saveAsGrayscale = false,
                        compressionQuality = 80,
                        desiredOutputSize = Size(200, 100),
                    ),
                )
            }
        }
    }

    private fun submitJob(documentFile: File) {
    }

    fun submitJob() {
        submitJob(documentFile = documentFile!!)
    }

    fun onDocumentRejected(isBackSide: Boolean) {
        _uiState.update {
            if (isBackSide) it.copy(backDocumentImageToConfirm = null) else it.copy(frontDocumentImageToConfirm = null)
        }
        documentFile?.delete()?.also { deleted ->
            if (!deleted) Timber.w("Failed to delete $documentFile")
        }
        documentFile = null
        result = null
    }
}
