package com.smileidentity.viewmodel.document

import android.graphics.ImageFormat.YUV_420_888
import androidx.annotation.StringRes
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.util.createDocumentFile
import com.smileidentity.util.postProcessImage
import com.smileidentity.util.toByteArray
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

private const val INTRA_IMAGE_MIN_DELAY_MS = 350
private const val LUMINOSITY_THRESHOLD = 35

data class DocumentCaptureUiState(
    val acknowledgedInstructions: Boolean = false,
    val directive: DocumentDirective = DocumentDirective.DefaultInstructions,
    val areEdgesDetected: Boolean = false,
    val showManualCaptureButton: Boolean = false,
    val documentImageToConfirm: File? = null,
    val captureError: Throwable? = null,
    val showCaptureInProgress: Boolean = false,
)

enum class DocumentDirective(@StringRes val displayText: Int) {
    DefaultInstructions(R.string.si_doc_v_capture_directive_default),
    EnsureWellLit(R.string.si_doc_v_capture_directive_ensure_well_lit),
    Capturing(R.string.si_doc_v_capture_directive_capturing),
}

class DocumentCaptureViewModel : ViewModel(), ImageAnalysis.Analyzer {
    private val _uiState = MutableStateFlow(DocumentCaptureUiState())
    val uiState = _uiState.asStateFlow()
    private var lastAutoCaptureTimeMs = 0L

    init {
        // Show manual capture after 10 seconds, using coroutine
        viewModelScope.launch {
            delay(10.seconds)
            _uiState.update { it.copy(showManualCaptureButton = true) }
        }
    }

    fun onInstructionsAcknowledged() {
        _uiState.update { it.copy(acknowledgedInstructions = true) }
    }

    fun onPhotoSelectedFromGallery(selectedPhoto: File?) {
        if (selectedPhoto == null) {
            val throwable = IllegalStateException("selectedPhoto is null")
            Timber.w(throwable)
            _uiState.update { it.copy(captureError = throwable) }
        } else {
            _uiState.update {
                it.copy(acknowledgedInstructions = true, documentImageToConfirm = selectedPhoto)
            }
        }
    }

    /**
     * To be called when auto capture determines the image quality is sufficient or when the user
     * taps the manual capture button.
     */
    fun captureDocument(cameraState: CameraState) {
        if (uiState.value.showCaptureInProgress) {
            Timber.v("Already capturing. Skipping duplicate capture request")
            return
        }
        _uiState.update {
            it.copy(showCaptureInProgress = true, directive = DocumentDirective.Capturing)
        }
        val documentFile = createDocumentFile()
        cameraState.takePicture(documentFile) { result ->
            when (result) {
                is ImageCaptureResult.Success -> {
                    _uiState.update {
                        it.copy(
                            documentImageToConfirm = postProcessImage(documentFile),
                            showCaptureInProgress = false,
                        )
                    }
                }

                is ImageCaptureResult.Error -> {
                    Timber.e("Error capturing document", result.throwable)
                    _uiState.update {
                        it.copy(captureError = result.throwable, showCaptureInProgress = false)
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
                captureError = null,
                documentImageToConfirm = null,
                acknowledgedInstructions = false,
                directive = DocumentDirective.DefaultInstructions,
            )
        }
    }

    override fun analyze(imageProxy: ImageProxy) = imageProxy.use {
        // YUV_420_888 is the format produced by CameraX
        check(imageProxy.format == YUV_420_888) { "Unsupported format: ${imageProxy.format}" }

        val elapsedTimeMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        if (uiState.value.showCaptureInProgress || elapsedTimeMs < INTRA_IMAGE_MIN_DELAY_MS) {
            return
        }
        lastAutoCaptureTimeMs = System.currentTimeMillis()

        // planes[0] is the Y plane aka "luma"
        val data = imageProxy.planes[0].buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luminance = pixels.average()

        if (luminance < LUMINOSITY_THRESHOLD) {
            _uiState.update { it.copy(directive = DocumentDirective.EnsureWellLit) }
        } else {
            _uiState.update { it.copy(directive = DocumentDirective.DefaultInstructions) }
        }
    }
}
