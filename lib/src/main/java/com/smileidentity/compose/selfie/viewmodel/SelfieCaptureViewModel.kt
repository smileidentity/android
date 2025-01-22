package com.smileidentity.compose.selfie.viewmodel

import androidx.annotation.OptIn
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.R
import com.smileidentity.models.v2.LivenessType
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.models.v2.SelfieImageOriginValue.BackCamera
import com.smileidentity.models.v2.SelfieImageOriginValue.FrontCamera
import com.smileidentity.util.StringResource
import com.smileidentity.util.area
import com.smileidentity.util.createLivenessFile
import com.smileidentity.util.createSelfieFile
import com.smileidentity.util.postProcessImageBitmap
import com.smileidentity.util.rotated
import com.ujizin.camposer.state.CamSelector
import java.io.File
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import timber.log.Timber

private val UI_DEBOUNCE_DURATION = 250.milliseconds
private const val INTRA_IMAGE_MIN_DELAY_MS = 350
private const val NUM_LIVENESS_IMAGES = 7
private const val TOTAL_STEPS = NUM_LIVENESS_IMAGES + 1 // 7 B&W Liveness + 1 Color Selfie
private const val LIVENESS_IMAGE_SIZE = 320
private const val SELFIE_IMAGE_SIZE = 640
private const val NO_FACE_RESET_DELAY_MS = 3000
private const val FACE_ROTATION_THRESHOLD = 0.75f
private const val MIN_FACE_AREA_THRESHOLD = 0.15f
const val MAX_FACE_AREA_THRESHOLD = 0.30f
private const val SMILE_THRESHOLD = 0.8f

sealed class SelfieCaptureResult {
    data class Success(
        val selfieFile: File,
        val livenessFiles: List<File>,
        val message: StringResource,
    ) : SelfieCaptureResult()

    data class Error(
        val message: StringResource = StringResource.ResId(R.string.si_processing_error_subtitle),
        val exception: Exception? = null,
    ) : SelfieCaptureResult()
}

enum class SelfieDirective(@StringRes val displayText: Int) {
    InitialInstruction(R.string.si_smart_selfie_instructions),
    Capturing(R.string.si_smart_selfie_directive_capturing),
    EnsureFaceInFrame(R.string.si_smart_selfie_directive_unable_to_detect_face),
    EnsureOnlyOneFace(R.string.si_smart_selfie_directive_multiple_faces),
    MoveCloser(R.string.si_smart_selfie_directive_face_too_far),
    MoveAway(R.string.si_smart_selfie_directive_face_too_close),
    Smile(R.string.si_smart_selfie_directive_smile),
}

data class SelfieCaptureUiState(
    val directive: SelfieDirective = SelfieDirective.InitialInstruction,
    val progress: Float = 0f,
    val result: SelfieCaptureResult? = null,
)

class SelfieCaptureViewModel(
    private val jobId: String,
    private val metadata: MutableList<Metadatum>,
    private val onResult: (SelfieCaptureResult) -> Unit,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelfieCaptureUiState())

    // Debounce to avoid spamming SelfieDirective updates so that they can be read by the user
    @kotlin.OptIn(FlowPreview::class)
    val uiState = _uiState.asStateFlow().debounce(UI_DEBOUNCE_DURATION).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        SelfieCaptureUiState(),
    )

    private val livenessFiles = mutableListOf<File>()
    private var lastAutoCaptureTimeMs = 0L
    private var previousHeadRotationX = Float.POSITIVE_INFINITY
    private var previousHeadRotationY = Float.POSITIVE_INFINITY
    private var previousHeadRotationZ = Float.POSITIVE_INFINITY

    @VisibleForTesting
    internal var shouldAnalyzeImages = true

    private val faceDetectorOptions = FaceDetectorOptions.Builder().apply {
        setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    }.build()
    private val faceDetector by lazy { FaceDetection.getClient(faceDetectorOptions) }

    private val metadataTimerStart = TimeSource.Monotonic.markNow()

    private fun setCameraFacingMetadata(camSelector: CamSelector) {
        metadata.removeAll { it is Metadatum.SelfieImageOrigin }
        when (camSelector) {
            CamSelector.Front -> metadata.add(Metadatum.SelfieImageOrigin(FrontCamera))
            CamSelector.Back -> metadata.add(Metadatum.SelfieImageOrigin(BackCamera))
        }
    }

    @OptIn(ExperimentalGetImage::class)
    internal fun analyzeImage(imageProxy: ImageProxy, camSelector: CamSelector) {
        val image = imageProxy.image
        val elapsedTimeMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        if (!shouldAnalyzeImages || elapsedTimeMs < INTRA_IMAGE_MIN_DELAY_MS || image == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            if (faces.isEmpty()) {
                _uiState.update { it.copy(directive = SelfieDirective.EnsureFaceInFrame) }
                // If no faces are detected for a while, reset the state
                if (elapsedTimeMs > NO_FACE_RESET_DELAY_MS) {
                    resetSelfieCaptureState()
                }
                return@addOnSuccessListener
            }

            // Ensure only 1 face is in frame
            if (faces.size > 1) {
                _uiState.update { it.copy(directive = SelfieDirective.EnsureOnlyOneFace) }
                return@addOnSuccessListener
            }

            val face = faces.first()

            // Check that the corners of the face bounding box are within the inputImage
            val faceCornersInImage = face.boundingBox.left >= 0 &&
                face.boundingBox.right <= inputImage.width &&
                face.boundingBox.top >= 0 &&
                face.boundingBox.bottom <= inputImage.height
            if (!faceCornersInImage) {
                _uiState.update { it.copy(directive = SelfieDirective.EnsureFaceInFrame) }
                return@addOnSuccessListener
            }

            // Check that the face is close enough to the camera
            val faceFillRatio = (face.boundingBox.area / inputImage.area.toFloat())
            if (faceFillRatio < MIN_FACE_AREA_THRESHOLD) {
                _uiState.update { it.copy(directive = SelfieDirective.MoveCloser) }
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            if (faceFillRatio > MAX_FACE_AREA_THRESHOLD) {
                _uiState.update { it.copy(directive = SelfieDirective.MoveAway) }
                return@addOnSuccessListener
            }

            // Ask the user to start smiling partway through liveness images
            val isSmiling = (face.smilingProbability ?: 0f) > SMILE_THRESHOLD
            if (livenessFiles.size > NUM_LIVENESS_IMAGES / 2 && !isSmiling) {
                _uiState.update { it.copy(directive = SelfieDirective.Smile) }
                return@addOnSuccessListener
            }

            _uiState.update { it.copy(directive = SelfieDirective.Capturing) }

            // Perform the rotation checks *after* changing directive to Capturing -- we don't want
            // to explicitly tell the user to move their head
            if (!hasFaceRotatedEnough(face)) {
                Timber.v("Not enough face rotation between captures. Waiting...")
                return@addOnSuccessListener
            }
            previousHeadRotationX = face.headEulerAngleX
            previousHeadRotationY = face.headEulerAngleY
            previousHeadRotationZ = face.headEulerAngleZ

            // All conditions satisfied, capture the image

            val bitmap = imageProxy.toBitmap().rotated(imageProxy.imageInfo.rotationDegrees)
            lastAutoCaptureTimeMs = System.currentTimeMillis()
            if (livenessFiles.size < NUM_LIVENESS_IMAGES) {
                val livenessFile = createLivenessFile(jobId)
                Timber.v("Capturing liveness image to $livenessFile")
                postProcessImageBitmap(
                    bitmap = bitmap,
                    file = livenessFile,
                    compressionQuality = 80,
                    resizeLongerDimensionTo = LIVENESS_IMAGE_SIZE,
                )
                livenessFiles.add(livenessFile)
                _uiState.update { it.copy(progress = livenessFiles.size / TOTAL_STEPS.toFloat()) }
            } else {
                val selfieFile = createSelfieFile(jobId)
                Timber.v("Capturing selfie image to $selfieFile")
                postProcessImageBitmap(
                    bitmap = bitmap,
                    file = selfieFile,
                    compressionQuality = 80,
                    resizeLongerDimensionTo = SELFIE_IMAGE_SIZE,
                )
                shouldAnalyzeImages = false
                setCameraFacingMetadata(camSelector)
                metadata.add(Metadatum.ActiveLivenessType(LivenessType.Smile))
                metadata.add(Metadatum.SelfieCaptureDuration(metadataTimerStart.elapsedNow()))
                val response = SelfieCaptureResult.Success(
                    selfieFile = selfieFile,
                    livenessFiles = livenessFiles,
                    message = StringResource.ResId(
                        R.string.si_smart_selfie_processing_success_subtitle,
                    ),
                )
                _uiState.update {
                    it.copy(
                        progress = 1f,
                        result = response,
                    )
                }
                onResult(response)
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error detecting faces")
            val response = SelfieCaptureResult.Error(
                message = StringResource.ResId(R.string.si_processing_error_subtitle),
                exception = exception,
            )
            _uiState.update {
                it.copy(
                    result = response,
                )
            }
            onResult(response)
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private fun hasFaceRotatedEnough(face: Face): Boolean {
        val rotationXDelta = (face.headEulerAngleX - previousHeadRotationX).absoluteValue
        val rotationYDelta = (face.headEulerAngleY - previousHeadRotationY).absoluteValue
        val rotationZDelta = (face.headEulerAngleZ - previousHeadRotationZ).absoluteValue
        return rotationXDelta > FACE_ROTATION_THRESHOLD ||
            rotationYDelta > FACE_ROTATION_THRESHOLD ||
            rotationZDelta > FACE_ROTATION_THRESHOLD
    }

    fun onSelfieRejected() {
        resetSelfieCaptureState()
    }

    private fun resetSelfieCaptureState() {
        when (val result = uiState.value.result) {
            is SelfieCaptureResult.Success -> {
                result.selfieFile.delete()
                result.livenessFiles.forEach { it.delete() }
                livenessFiles.clear()
            }

            else -> {
                /* No files to clean up */
            }
        }

        _uiState.update {
            it.copy(
                directive = SelfieDirective.InitialInstruction,
                progress = 0f,
                result = null,
            )
        }

        shouldAnalyzeImages = true
    }
}
