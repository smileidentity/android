package com.smileidentity.viewmodel.document

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController.TAP_TO_FOCUS_NOT_FOCUSED
import androidx.camera.view.CameraController.TAP_TO_FOCUS_STARTED
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.smileidentity.R
import com.smileidentity.compose.document.DocumentCaptureSide
import com.smileidentity.models.v2.DocumentImageOriginValue
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.util.calculateLuminance
import com.smileidentity.util.createDocumentFile
import com.smileidentity.util.postProcessImage
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import java.io.File
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

private const val ANALYSIS_SAMPLE_INTERVAL_MS = 350
private const val LUMINANCE_THRESHOLD = 35
private const val CORRECT_ASPECT_RATIO_TOLERANCE = 0.1f
private const val CENTERED_BOUNDING_BOX_TOLERANCE = 30
private const val DOCUMENT_AUTO_CAPTURE_WAIT_TIME_MS = 1_000L

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
    private val jobId: String,
    private val side: DocumentCaptureSide,
    private val knownAspectRatio: Float?,
    private val metadata: MutableList<Metadatum>,
    private val objectDetector: ObjectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .build(),
    ),
) : ViewModel() {
    var documentImageOrigin: DocumentImageOriginValue? = null
        private set
    private val _uiState = MutableStateFlow(DocumentCaptureUiState())
    val uiState = _uiState.asStateFlow()
    private var lastAnalysisTimeMs = 0L
    private var isCapturing = false
    private var isFocusing = false
    private var documentFirstDetectedTimeMs: Long? = null
    private var captureNextAnalysisFrame = false
    private val defaultAspectRatio = knownAspectRatio ?: 1f
    private var retryCount = 0
    private val timerStart = TimeSource.Monotonic.markNow()

    init {
        _uiState.update { it.copy(idAspectRatio = defaultAspectRatio) }

        // Show manual capture after 10 seconds
        viewModelScope.launch {
            delay(10.seconds)
            _uiState.update { it.copy(showManualCaptureButton = true) }
        }

        viewModelScope.launch {
            uiState.collect {
                if (it.areEdgesDetected) {
                    documentFirstDetectedTimeMs?.let {
                        if (System.currentTimeMillis() - it > DOCUMENT_AUTO_CAPTURE_WAIT_TIME_MS) {
                            captureNextAnalysisFrame = true
                        }
                    }
                    if (documentFirstDetectedTimeMs == null) {
                        documentFirstDetectedTimeMs = System.currentTimeMillis()
                    }
                } else {
                    documentFirstDetectedTimeMs = null
                }
            }
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
            documentImageOrigin = DocumentImageOriginValue.Gallery
            _uiState.update {
                it.copy(acknowledgedInstructions = true, documentImageToConfirm = selectedPhoto)
            }
        }
    }

    fun captureDocumentManually(cameraState: CameraState) {
        documentImageOrigin = DocumentImageOriginValue.CameraManualCapture
        captureDocument(cameraState)
    }

    /**
     * To be called when auto capture determines the image quality is sufficient or when the user
     * taps the manual capture button.
     */
    fun captureDocument(cameraState: CameraState) {
        if (isCapturing || uiState.value.documentImageToConfirm != null) {
            Timber.v("Already capturing. Skipping duplicate capture request")
            return
        }
        isCapturing = true
        _uiState.update {
            it.copy(showCaptureInProgress = true, directive = DocumentDirective.Capturing)
        }
        val documentFile = createDocumentFile(jobId, (side == DocumentCaptureSide.Front))
        cameraState.takePicture(documentFile) { result ->
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
            // NB: This should be set to false last after processing is done for the captured image
            isCapturing = false
        }
    }

    fun onRetry() {
        // It is safe to delete the file here, even though it may have been selected from the
        // gallery because we copied the URI contents to a new File first
        uiState.value.documentImageToConfirm?.delete()
        when (side) {
            DocumentCaptureSide.Front -> {
                metadata.removeAll { it is Metadatum.DocumentFrontCaptureRetries }
                metadata.removeAll { it is Metadatum.DocumentFrontCaptureDuration }
                metadata.removeAll { it is Metadatum.DocumentFrontImageOrigin }
            }

            DocumentCaptureSide.Back -> {
                metadata.removeAll { it is Metadatum.DocumentBackCaptureRetries }
                metadata.removeAll { it is Metadatum.DocumentBackCaptureDuration }
                metadata.removeAll { it is Metadatum.DocumentBackImageOrigin }
            }
        }
        isCapturing = false
        documentImageOrigin = null
        retryCount++
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

    fun onConfirm(documentImageToConfirm: File, onConfirm: (File) -> Unit) {
        val elapsed = timerStart.elapsedNow()
        when (side) {
            DocumentCaptureSide.Front -> {
                metadata.add(Metadatum.DocumentFrontCaptureRetries(retryCount))
                metadata.add(Metadatum.DocumentFrontCaptureDuration(elapsed))
                documentImageOrigin?.let { metadata.add(Metadatum.DocumentFrontImageOrigin(it)) }
            }

            DocumentCaptureSide.Back -> {
                metadata.add(Metadatum.DocumentBackCaptureRetries(retryCount))
                metadata.add(Metadatum.DocumentBackCaptureDuration(elapsed))
                documentImageOrigin?.let { metadata.add(Metadatum.DocumentBackImageOrigin(it)) }
            }
        }
        onConfirm(documentImageToConfirm)
    }

    fun onFocusEvent(focusEvent: Int) {
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

    @OptIn(ExperimentalGetImage::class)
    fun analyze(imageProxy: ImageProxy, cameraState: CameraState) {
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
                    imageRotation = rotation,
                )

                val detectedAspectRatio = bBox.width().toFloat() / bBox.height()
                val isCorrectAspectRatio = isCorrectAspectRatio(
                    detectedAspectRatio = detectedAspectRatio,
                )
                val idAspectRatio = if (rotation == 90 || rotation == 270) {
                    knownAspectRatio ?: detectedAspectRatio
                } else {
                    1 / (knownAspectRatio ?: detectedAspectRatio)
                }

                val areEdgesDetected = isCentered && isCorrectAspectRatio
                _uiState.update {
                    it.copy(
                        areEdgesDetected = areEdgesDetected,
                        idAspectRatio = idAspectRatio,
                    )
                }

                if (captureNextAnalysisFrame && areEdgesDetected && !isCapturing && !isFocusing &&
                    uiState.value.documentImageToConfirm == null
                ) {
                    captureNextAnalysisFrame = false
                    documentImageOrigin = DocumentImageOriginValue.CameraAutoCapture
                    captureDocument(cameraState)
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

    private fun isBoundingBoxCentered(
        boundingBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
        imageRotation: Int,
        tolerance: Int = CENTERED_BOUNDING_BOX_TOLERANCE,
    ): Boolean {
        if (imageRotation == 90 || imageRotation == 270) {
            // The image height/width need to be swapped
            return isBoundingBoxCentered(
                boundingBox = boundingBox,
                imageWidth = imageHeight,
                imageHeight = imageWidth,
                imageRotation = 0,
                tolerance = tolerance,
            )
        }

        // Sometimes, the bounding box is out of frame. This cannot be considered centered
        // We check only left and right because the document should always fill the width but may
        // not fill the height
        if (boundingBox.left < tolerance || boundingBox.right > (imageWidth - tolerance)) {
            return false
        }

        val imageCenterX = imageWidth / 2
        val imageCenterY = imageHeight / 2

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
