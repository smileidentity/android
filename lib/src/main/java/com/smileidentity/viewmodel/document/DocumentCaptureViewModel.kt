package com.smileidentity.viewmodel.document

import android.graphics.ImageFormat.YUV_420_888
import androidx.annotation.StringRes
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController.TAP_TO_FOCUS_NOT_FOCUSED
import androidx.camera.view.CameraController.TAP_TO_FOCUS_STARTED
import androidx.camera.view.CameraController.TapToFocusStates
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
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
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

private const val ANALYSIS_SAMPLE_INTERVAL_MS = 350
private const val LUMINOSITY_THRESHOLD = 35

data class DocumentCaptureUiState(
    val acknowledgedInstructions: Boolean = false,
    val directive: DocumentDirective = DocumentDirective.DefaultInstructions,
    val areEdgesDetected: Boolean = false,
    val idAspectRatio: Float = 1f,
    val showManualCaptureButton: Boolean = false,
    val documentImageToConfirm: File? = null,
    val captureError: Throwable? = null,
    val showCaptureInProgress: Boolean = false,
)

enum class DocumentDirective(@StringRes val displayText: Int) {
    DefaultInstructions(R.string.si_doc_v_capture_directive_default),
    EnsureWellLit(R.string.si_doc_v_capture_directive_ensure_well_lit),
    Focusing(R.string.si_doc_v_capture_directive_focusing),
    Capturing(R.string.si_doc_v_capture_directive_capturing),
}

class DocumentCaptureViewModel(
    private val knownAspectRatio: Float?,
) : ViewModel(), ImageAnalysis.Analyzer {
    private val _uiState = MutableStateFlow(DocumentCaptureUiState())
    val uiState = _uiState.asStateFlow()
    private var lastAnalysisTimeMs = 0L
    private var isCapturing = false
    private var isFocusing = false
    private val defaultAspectRatio get() = knownAspectRatio ?: 1f

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .build(),
    )

    init {
        _uiState.update { it.copy(idAspectRatio = defaultAspectRatio) }

        // Show manual capture after 10 seconds
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
        if (isCapturing) {
            Timber.v("Already capturing. Skipping duplicate capture request")
            return
        }
        isCapturing = true
        _uiState.update {
            it.copy(showCaptureInProgress = true, directive = DocumentDirective.Capturing)
        }
        val documentFile = createDocumentFile()
        cameraState.takePicture(documentFile) { result ->
            isCapturing = false
            when (result) {
                is ImageCaptureResult.Success -> {
                    _uiState.update {
                        it.copy(
                            documentImageToConfirm = postProcessImage(
                                documentFile,
                                desiredAspectRatio = uiState.value.idAspectRatio,
                            ),
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
                areEdgesDetected = false,
            )
        }
    }

    fun onFocusEvent(@TapToFocusStates focusEvent: Int) {
        isFocusing = focusEvent == TAP_TO_FOCUS_STARTED || focusEvent == TAP_TO_FOCUS_NOT_FOCUSED
        if (isFocusing) {
            _uiState.update { it.copy(directive = DocumentDirective.Focusing) }
        }
    }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        // YUV_420_888 is the format produced by CameraX
        check(imageProxy.format == YUV_420_888) { "Unsupported format: ${imageProxy.format}" }
        val image = imageProxy.image
        val elapsedTimeMs = System.currentTimeMillis() - lastAnalysisTimeMs
        // When capturing or focusing, skip performing image analysis
        if (isCapturing) {
            _uiState.update { it.copy(directive = DocumentDirective.Capturing) }
            imageProxy.close()
        } else if (isFocusing) {
            _uiState.update {
                it.copy(directive = DocumentDirective.Focusing, areEdgesDetected = false)
            }
            imageProxy.close()
        } else if (elapsedTimeMs < ANALYSIS_SAMPLE_INTERVAL_MS || image == null) {
            imageProxy.close()
            return
        } else {
            lastAnalysisTimeMs = System.currentTimeMillis()

            // planes[0] is the Y plane aka "luma"
            val data = imageProxy.planes[0].buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luminance = pixels.average()

            if (luminance < LUMINOSITY_THRESHOLD) {
                _uiState.update {
                    it.copy(directive = DocumentDirective.EnsureWellLit, areEdgesDetected = false)
                }
                imageProxy.close()
            } else {
                _uiState.update { it.copy(directive = DocumentDirective.DefaultInstructions) }
                val rotation = imageProxy.imageInfo.rotationDegrees
                val inputImage = InputImage.fromMediaImage(image, rotation)
                objectDetector.process(inputImage)
                    .addOnSuccessListener {
                        if (it.isEmpty()) {
                            resetBoundingBox()
                        } else {
                            val boundingBox = it.first().boundingBox
                            Timber.v("Detected object: $boundingBox")
                            val detectedAspectRatio =
                                boundingBox.width().toFloat() / boundingBox.height()
                            val expectedAspectRatio = knownAspectRatio ?: defaultAspectRatio
                            // todo: ensure bounding box is in the center
                            if (abs(detectedAspectRatio - expectedAspectRatio) < 0.1) {
                                _uiState.update {
                                    it.copy(
                                        areEdgesDetected = true,
                                        idAspectRatio = detectedAspectRatio,
                                    )
                                }
                            } else {
                                resetBoundingBox()
                            }
                        }
                    }
                    .addOnFailureListener {
                        resetBoundingBox()
                        Timber.e(it)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
                return
            }
        }
    }

    private fun resetBoundingBox() {
        _uiState.update {
            it.copy(areEdgesDetected = false, idAspectRatio = defaultAspectRatio)
        }
    }
}
