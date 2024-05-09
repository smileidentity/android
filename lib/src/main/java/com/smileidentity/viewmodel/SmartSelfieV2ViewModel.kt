package com.smileidentity.viewmodel

import android.graphics.Bitmap
import android.graphics.ImageFormat.YUV_420_888
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.networking.doSmartSelfieAuthentication
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.area
import com.smileidentity.util.calculateLuminance
import com.smileidentity.util.createLivenessFile
import com.smileidentity.util.createSelfieFile
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.postProcessImageBitmap
import com.smileidentity.util.rotated
import java.io.File
import kotlin.math.absoluteValue
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import timber.log.Timber

private const val HISTORY_LENGTH = 7
private const val INTRA_IMAGE_MIN_DELAY_MS = 250
private const val NUM_LIVENESS_IMAGES = 6
private const val LIVENESS_IMAGE_SIZE = 320
private const val SELFIE_IMAGE_SIZE = 640
private const val FACE_QUALITY_THRESHOLD = 0.5f
private const val MIN_FACE_FILL_THRESHOLD = 0.15f
private const val MAX_FACE_FILL_THRESHOLD = 0.25f
private const val LUMINANCE_THRESHOLD = 50
private const val MAX_FACE_PITCH_THRESHOLD = 30
private const val MAX_FACE_YAW_THRESHOLD = 15
private const val MAX_FACE_ROLL_THRESHOLD = 30
private const val LIVENESS_STABILITY_TIME_MS = 300

enum class SelfieHint(@DrawableRes val animation: Int) {
    SearchingForFace(R.drawable.si_tf_face_search),
    NeedLight(R.drawable.si_tf_light_flash),
}

enum class FaceDirection {
    Left,
    Right,
    Up,
}

data class SmartSelfieV2UiState(
    val backgroundOpacity: Float = 0.8f,
    val cutoutOpacity: Float = 0.8f,
    val showBorderHighlight: Boolean = false,
    val selfieHint: SelfieHint? = SelfieHint.SearchingForFace,
    val showLoading: Boolean = false,
    val showCompletion: Boolean = false,
    val faceDirectionHint: FaceDirection? = null,
)

@kotlin.OptIn(FlowPreview::class)
class SmartSelfieV2ViewModel(
    private val userId: String,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    private val selfieQualityModel: SelfieQualityModel,
    private val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().apply {
            setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        }.build(),
    ),
    private val onResult: SmileIDCallback<SmartSelfieResult>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SmartSelfieV2UiState())
    val uiState = _uiState.asStateFlow().sample(250).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        SmartSelfieV2UiState(),
    )
    private val livenessFiles = mutableListOf<File>()
    private var selfieFile: File? = null
    private var lastAutoCaptureTimeMs = 0L
    private var shouldAnalyzeImages = true
    private val modelInputSize = intArrayOf(1, 120, 120, 3)
    private val selfieQualityHistory = mutableListOf<Float>()
    private val orderedFaceDirections = FaceDirection.entries.shuffled()
    private var initialLivenessSatisfiedTimeMs = Long.MAX_VALUE

    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy) {
        val elapsedTimeMs = System.currentTimeMillis() - lastAutoCaptureTimeMs
        val enoughTimeHasPassed = elapsedTimeMs > INTRA_IMAGE_MIN_DELAY_MS
        if (!enoughTimeHasPassed || !shouldAnalyzeImages) {
            imageProxy.close()
            return
        }

        val image = imageProxy.image ?: run {
            Timber.w("ImageProxy has no image")
            SmileIDCrashReporting.hub.addBreadcrumb("ImageProxy has no image")
            imageProxy.close()
            return
        }

        // YUV_420_888 is the format produced by CameraX and needed for Luminance calculation
        check(imageProxy.format == YUV_420_888) { "Unsupported format: ${imageProxy.format}" }
        val luminance = calculateLuminance(imageProxy)
        if (luminance < LUMINANCE_THRESHOLD) {
            Timber.d("Low luminance detected")
            _uiState.update {
                it.copy(
                    showBorderHighlight = false,
                    cutoutOpacity = 1f,
                    selfieHint = SelfieHint.NeedLight,
                )
            }
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            val face = faces.firstOrNull() ?: run {
                Timber.d("No face detected")
                resetCaptureProgress()
                return@addOnSuccessListener
            }

            if (faces.size > 1) {
                Timber.d("More than one face detected")
                resetCaptureProgress()
                return@addOnSuccessListener
            }

            val bBox = face.boundingBox

            // Check that the corners of the face bounding box are within the inputImage
            val faceCornersInImage = bBox.left >= 0 && bBox.right <= inputImage.width &&
                bBox.top >= 0 && bBox.bottom <= inputImage.height
            if (!faceCornersInImage) {
                Timber.d("Face bounding box not within image")
                resetCaptureProgress()
                return@addOnSuccessListener
            }

            // Check that the face is close enough to the camera
            val faceFillRatio = (face.boundingBox.area / inputImage.area.toFloat())
            if (faceFillRatio < MIN_FACE_FILL_THRESHOLD) {
                Timber.d("Face not close enough to camera")
                resetCaptureProgress()
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            if (faceFillRatio > MAX_FACE_FILL_THRESHOLD) {
                Timber.d("Face too close to camera")
                resetCaptureProgress()
                return@addOnSuccessListener
            }

            val fullSelfieBmp = imageProxy.toBitmap().rotated(imageProxy.imageInfo.rotationDegrees)
            if (bBox.left + bBox.width() > fullSelfieBmp.width) {
                Timber.d("Face bounding box width is greater than image width")
                resetCaptureProgress()
                return@addOnSuccessListener
            }
            if (bBox.top + bBox.height() > fullSelfieBmp.height) {
                Timber.d("Face bounding box height is greater than image height")
                resetCaptureProgress()
                return@addOnSuccessListener
            }

            val selfieFile = this.selfieFile // for smart casting purposes
            if (selfieFile == null) {
                // Reject extreme head poses
                val extremePitch = face.headEulerAngleX.absoluteValue > MAX_FACE_PITCH_THRESHOLD
                val extremeYaw = face.headEulerAngleY.absoluteValue > MAX_FACE_YAW_THRESHOLD
                val extremeRoll = face.headEulerAngleZ.absoluteValue > MAX_FACE_ROLL_THRESHOLD
                if (extremePitch || extremeYaw || extremeRoll) {
                    Timber.d("Extreme head pose detected")
                    resetCaptureProgress()
                    return@addOnSuccessListener
                }
                // We only run the image quality model on the selfie capture because the liveness
                // task requires a turned head, which receives a lower score from the model

                // Image Quality Model Inference
                // Model input: nx120x120x3 - n images, each cropped to face bounding box
                // Model output: nx2 - n images, each with 2 probabilities
                // 1st column is the actual quality. 2nd column is 1-(1st_column)

                // NB! Model is trained on *face mesh* crop (potentially different from face
                // detection)
                val input = TensorImage(DataType.FLOAT32).apply {
                    val modelInputBmp = Bitmap.createBitmap(
                        fullSelfieBmp,
                        bBox.left,
                        bBox.top,
                        bBox.width(),
                        bBox.height(),
                        // NB! bBox is not guaranteed to be square, so scale might squish the image
                    ).scale(modelInputSize[1], modelInputSize[2], false)
                    load(modelInputBmp)
                }
                val outputs = selfieQualityModel.process(input.tensorBuffer)
                val output = outputs.outputFeature0AsTensorBuffer.floatArray.firstOrNull() ?: run {
                    Timber.w("No image quality output")
                    resetCaptureProgress()
                    return@addOnSuccessListener
                }
                selfieQualityHistory.add(output)
                if (selfieQualityHistory.size > HISTORY_LENGTH) {
                    // We should only ever exceed history length by 1
                    selfieQualityHistory.removeAt(0)
                }

                val averageFaceQuality = selfieQualityHistory.average()

                if (averageFaceQuality < FACE_QUALITY_THRESHOLD) {
                    // We don't want to reset the history here, since the model output is noisy
                    Timber.d("Face quality not met ($averageFaceQuality)")
                    _uiState.update { it.copy(showBorderHighlight = false, cutoutOpacity = 0.8f) }
                    return@addOnSuccessListener
                }
                _uiState.update {
                    it.copy(
                        showBorderHighlight = true,
                        cutoutOpacity = 0f,
                        selfieHint = null,
                        faceDirectionHint = orderedFaceDirections.first(),
                    )
                }
                lastAutoCaptureTimeMs = System.currentTimeMillis()
                // local variable is for null type safety purposes
                val selfieFile = createSelfieFile(userId)
                this.selfieFile = selfieFile
                Timber.v("Capturing selfie image to $selfieFile")
                postProcessImageBitmap(
                    bitmap = fullSelfieBmp,
                    file = selfieFile,
                    compressionQuality = 80,
                    resizeLongerDimensionTo = SELFIE_IMAGE_SIZE,
                )
                return@addOnSuccessListener
            }

            if (!shouldCaptureLiveness(face)) {
                return@addOnSuccessListener
            }

            val livenessFile = createLivenessFile(userId)
            Timber.v("Capturing liveness image to $livenessFile")
            postProcessImageBitmap(
                bitmap = fullSelfieBmp,
                file = livenessFile,
                compressionQuality = 80,
                resizeLongerDimensionTo = LIVENESS_IMAGE_SIZE,
            )
            livenessFiles.add(livenessFile)

            if (livenessFiles.size < NUM_LIVENESS_IMAGES) {
                _uiState.update {
                    val index = (livenessFiles.size / 2) % orderedFaceDirections.size
                    it.copy(faceDirectionHint = orderedFaceDirections[index])
                }
                return@addOnSuccessListener
            }

            shouldAnalyzeImages = false
            val proxy = { e: Throwable -> onResult(SmileIDResult.Error(e)) }
            viewModelScope.launch(getExceptionHandler(proxy)) {
                var done = false
                // Start submitting the job right away, but show the spinner after a small delay
                // to make it feel like the API call is a bit faster
                awaitAll(
                    async {
                        val apiResponse = SmileID.api.doSmartSelfieAuthentication(
                            userId = userId,
                            selfieImage = selfieFile,
                            livenessImages = livenessFiles,
                            partnerParams = extraPartnerParams,
                        )
                        done = true
                        _uiState.update {
                            it.copy(
                                showLoading = false,
                                showCompletion = true,
                                backgroundOpacity = 0.99f,
                                showBorderHighlight = false,
                            )
                        }
                        // Delay to ensure the completion icon is shown for a little bit
                        delay(2500)
                        val result = SmartSelfieResult(selfieFile, livenessFiles, apiResponse)
                        onResult(SmileIDResult.Success(result))
                    },
                    async {
                        delay(1500)
                        if (!done) {
                            _uiState.update {
                                it.copy(
                                    showLoading = true,
                                    backgroundOpacity = 0.99f,
                                    showBorderHighlight = false,
                                    faceDirectionHint = null,
                                )
                            }
                        }
                    },
                )
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error analyzing image")
            SmileIDCrashReporting.hub.addBreadcrumb("Error analyzing image")
            onResult(SmileIDResult.Error(exception))
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private fun resetLivenessStabilityTime() {
        initialLivenessSatisfiedTimeMs = Long.MAX_VALUE
    }

    private fun resetCaptureProgress() {
        _uiState.update {
            it.copy(
                showBorderHighlight = false,
                cutoutOpacity = 0.8f,
                selfieHint = SelfieHint.SearchingForFace,
                faceDirectionHint = null,
            )
        }
        selfieQualityHistory.clear()
        livenessFiles.removeAll { it.delete() }
        selfieFile?.delete()
        selfieFile = null
    }

    private fun shouldCaptureLiveness(face: Face): Boolean {
        // For each direction the user is supposed to look, we capture 2 liveness images:
        // 1. At the midpoint of the direction
        // 2. At the end of the direction
        val index = (livenessFiles.size / 2) % orderedFaceDirections.size
        val currentActiveLivenessDirection = orderedFaceDirections[index]
        val shouldCaptureMidpoint = livenessFiles.size % 2 == 0
        val isLookingCorrectDirection = if (shouldCaptureMidpoint) {
            when (currentActiveLivenessDirection) {
                FaceDirection.Left -> isFaceLookingLeft(face, 10f)
                FaceDirection.Right -> isFaceLookingRight(face, 10f)
                FaceDirection.Up -> isFaceLookingUp(face, 7.5f)
            }
        } else {
            when (currentActiveLivenessDirection) {
                FaceDirection.Left -> isFaceLookingLeft(face)
                FaceDirection.Right -> isFaceLookingRight(face)
                FaceDirection.Up -> isFaceLookingUp(face)
            }
        }
        if (!isLookingCorrectDirection) {
            resetLivenessStabilityTime()
        }
        // Check that user has been looking in the correct direction for half a second for non-midpoint capture
        var hasBeenLookingLongEnough = true
        if (!shouldCaptureMidpoint) {
            if (initialLivenessSatisfiedTimeMs == Long.MAX_VALUE) {
                initialLivenessSatisfiedTimeMs = System.currentTimeMillis()
            }
            val elapsedTimeMs = System.currentTimeMillis() - initialLivenessSatisfiedTimeMs
            hasBeenLookingLongEnough = elapsedTimeMs > LIVENESS_STABILITY_TIME_MS
        }
        val result = isLookingCorrectDirection &&
            hasBeenLookingLongEnough &&
            livenessFiles.size < NUM_LIVENESS_IMAGES
        if (result) {
            resetLivenessStabilityTime()
        }
        return result
    }

    private fun isFaceLookingLeft(face: Face, qualifyingAngle: Float = 20f): Boolean {
        val result = face.headEulerAngleY > qualifyingAngle
        Timber.v("isFaceLookingLeft: $result")
        return result
    }

    private fun isFaceLookingRight(face: Face, qualifyingAngle: Float = 20f): Boolean {
        val result = face.headEulerAngleY < -qualifyingAngle
        Timber.v("isFaceLookingRight: $result")
        return result
    }

    private fun isFaceLookingUp(face: Face, qualifyingAngle: Float = 15f): Boolean {
        val result = face.headEulerAngleX > qualifyingAngle
        Timber.v("isFaceLookingUp: $result")
        return result
    }
}
