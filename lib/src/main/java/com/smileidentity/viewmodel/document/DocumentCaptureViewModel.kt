package com.smileidentity.viewmodel.document

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Rect
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
import com.google.mlkit.vision.objects.ObjectDetector
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
private const val LUMINANCE_THRESHOLD = 35
private const val CORRECT_ASPECT_RATIO_TOLERANCE = 0.1f
private const val CENTERED_BOUNDING_BOX_TOLERANCE = 30

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
    private val objectDetector: ObjectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .build(),
    ),
) : ViewModel(), ImageAnalysis.Analyzer {
    private val _uiState = MutableStateFlow(DocumentCaptureUiState())
    val uiState = _uiState.asStateFlow()
    private var lastAnalysisTimeMs = 0L
    private var isCapturing = false
    private var isFocusing = false
    private val defaultAspectRatio get() = knownAspectRatio ?: 1f

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
            _uiState.update {
                it.copy(
                    directive = DocumentDirective.Focusing,
                    areEdgesDetected = false,
                )
            }
        }
    }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        // YUV_420_888 is the format produced by CameraX
        check(imageProxy.format == YUV_420_888) { "Unsupported format: ${imageProxy.format}" }
        val image = imageProxy.image
        val elapsedTimeMs = System.currentTimeMillis() - lastAnalysisTimeMs
        val enoughTimeHasPassed = elapsedTimeMs > ANALYSIS_SAMPLE_INTERVAL_MS

        if (isCapturing || isFocusing || !enoughTimeHasPassed || image == null) {
            imageProxy.close()
            return
        }
        lastAnalysisTimeMs = System.currentTimeMillis()

        val luminance = calculateLuminance(imageProxy)
        if (luminance < LUMINANCE_THRESHOLD) {
            _uiState.update {
                it.copy(directive = DocumentDirective.EnsureWellLit, areEdgesDetected = false)
            }
            imageProxy.close()
            return
        }

        _uiState.update { it.copy(directive = DocumentDirective.DefaultInstructions) }
        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(image, rotation)
        objectDetector.process(inputImage)
            .addOnSuccessListener {
                if (it.isEmpty()) {
                    resetBoundingBox()
                    return@addOnSuccessListener
                }
                val bBox = it.first().boundingBox

                val isCentered = isBoundingBoxCentered(
                    boundingBox = bBox,
                    imageWidth = inputImage.width,
                    imageHeight = inputImage.height,
                )

                val detectedAspectRatio = bBox.width().toFloat() / bBox.height()
                val isCorrectAspectRatio = isCorrectAspectRatio(
                    detectedAspectRatio = detectedAspectRatio,
                )

                _uiState.update {
                    it.copy(
                        areEdgesDetected = isCentered && isCorrectAspectRatio,
                        idAspectRatio = knownAspectRatio ?: detectedAspectRatio,
                    )
                }
            }
            .addOnFailureListener {
                Timber.e(it)
                resetBoundingBox()
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun resetBoundingBox() {
        _uiState.update {
            it.copy(areEdgesDetected = false, idAspectRatio = defaultAspectRatio)
        }
    }

    private fun calculateLuminance(imageProxy: ImageProxy): Double {
        // planes[0] is the Y plane aka "luma"
        val data = imageProxy.planes[0].buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        return pixels.average()
    }

    private fun isBoundingBoxCentered(
        boundingBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
        tolerance: Int = CENTERED_BOUNDING_BOX_TOLERANCE,
    ): Boolean {
        // NB! The bounding box X dimension corresponds with the image *height* not width

        // Sometimes, the bounding box is out of frame. This cannot be considered centered
        // We check only left and right because the document should always fill the width but may
        // not fill the height
        if (boundingBox.left < tolerance || boundingBox.right > (imageHeight - tolerance)) {
            return false
        }

        val imageCenterX = imageHeight / 2
        val imageCenterY = imageWidth / 2

        val deltaX = abs(imageCenterX - boundingBox.centerX())
        val deltaY = abs(imageCenterY - boundingBox.centerY())

        val isCenteredHorizontally = deltaX < tolerance
        val isCenteredVertically = deltaY < tolerance

        return isCenteredHorizontally && isCenteredVertically
    }

    private fun isCorrectAspectRatio(
        detectedAspectRatio: Float,
        tolerance: Float = CORRECT_ASPECT_RATIO_TOLERANCE,
    ): Boolean {
        val expectedAspectRatio = knownAspectRatio ?: detectedAspectRatio
        return abs(detectedAspectRatio - expectedAspectRatio) < tolerance
    }
}
