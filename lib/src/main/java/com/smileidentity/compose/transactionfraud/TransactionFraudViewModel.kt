package com.smileidentity.compose.transactionfraud

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
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.ml.ImQualCp20Optimized
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.SmartSelfieJobResult
import com.smileidentity.networking.asFormDataPart
import com.smileidentity.networking.asFormDataParts
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
private const val NUM_LIVENESS_IMAGES = 4
private val LIVENESS_IMAGE_SIZE = android.util.Size(320, 320)
private val SELFIE_IMAGE_SIZE = android.util.Size(640, 640)
private const val FACE_QUALITY_THRESHOLD = 0.5f
private const val MIN_FACE_AREA_THRESHOLD = 0.15f
private const val MAX_FACE_AREA_THRESHOLD = 0.25f
private const val LUMINANCE_THRESHOLD = 50
private const val MAX_FACE_PITCH_THRESHOLD = 40
private const val MAX_FACE_YAW_THRESHOLD = 30
private const val MAX_FACE_ROLL_THRESHOLD = 40

enum class SelfieHint(@DrawableRes val animation: Int) {
    SearchingForFace(R.drawable.si_tf_face_search),
    NeedLight(R.drawable.si_tf_light_flash),
}

data class TransactionFraudUiState(
    val backgroundOpacity: Float = 0.8f,
    val cutoutOpacity: Float = 0.8f,
    val showBorderHighlight: Boolean = false,
    val selfieHint: SelfieHint? = SelfieHint.SearchingForFace,
    val showLoading: Boolean = false,
    val showCompletion: Boolean = false,
)

@kotlin.OptIn(FlowPreview::class)
class TransactionFraudViewModel(
    private val userId: String,
    private val jobId: String,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    private val imageQualityModel: ImQualCp20Optimized,
    private val faceDetector: FaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder().apply {
            setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        }.build(),
    ),
    private val onResult: SmileIDCallback<SmartSelfieJobResult.Entry>,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionFraudUiState())
    val uiState = _uiState.asStateFlow().sample(250).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        TransactionFraudUiState(),
    )
    private val livenessFiles = mutableListOf<File>()
    private var selfieFile: File? = null
    private var lastAutoCaptureTimeMs = 0L
    private var shouldAnalyzeImages = true
    private val modelInputSize = intArrayOf(1, 120, 120, 3)
    private val selfieQualityHistory = mutableListOf<Float>()

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
                resetFaceQuality()
                return@addOnSuccessListener
            }

            if (faces.size > 1) {
                Timber.d("More than one face detected")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            val bBox = face.boundingBox

            // Check that the corners of the face bounding box are within the inputImage
            val faceCornersInImage = bBox.left >= 0 && bBox.right <= inputImage.width &&
                bBox.top >= 0 && bBox.bottom <= inputImage.height
            if (!faceCornersInImage) {
                Timber.d("Face bounding box not within image")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            // Check that the face is close enough to the camera
            val faceFillRatio = (face.boundingBox.area / inputImage.area.toFloat())
            if (faceFillRatio < MIN_FACE_AREA_THRESHOLD) {
                Timber.d("Face not close enough to camera")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            // Check that the face is not too close to the camera
            if (faceFillRatio > MAX_FACE_AREA_THRESHOLD) {
                Timber.d("Face too close to camera")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            // Reject extreme head poses
            val extremePitch = face.headEulerAngleX.absoluteValue > MAX_FACE_PITCH_THRESHOLD
            val extremeYaw = face.headEulerAngleY.absoluteValue > MAX_FACE_YAW_THRESHOLD
            val extremeRoll = face.headEulerAngleZ.absoluteValue > MAX_FACE_ROLL_THRESHOLD
            if (extremePitch || extremeYaw || extremeRoll) {
                Timber.d("Extreme head pose detected")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            val fullSelfieBmp = imageProxy.toBitmap().rotated(imageProxy.imageInfo.rotationDegrees)
            if (bBox.left + bBox.width() > fullSelfieBmp.width) {
                Timber.d("Face bounding box width is greater than image width")
                resetFaceQuality()
                return@addOnSuccessListener
            }
            if (bBox.top + bBox.height() > fullSelfieBmp.height) {
                Timber.d("Face bounding box height is greater than image height")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            // Image Quality Model Inference
            // Model input: nx120x120x3 - n images, each cropped to face bounding box
            // Model output: nx2 - n images, each with 2 probabilities
            // 1st column is the actual quality. 2nd column is 1-(1st_column)

            // NB! Model is trained on *face mesh* crop (potentially different from face detection)
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
            // val modelStartTime = System.nanoTime()
            val outputs = imageQualityModel.process(input.tensorBuffer)
            // val modelElapsedTimeMs = (System.nanoTime() - modelStartTime) / 1_000_000
            val output = outputs.outputFeature0AsTensorBuffer.floatArray.firstOrNull() ?: run {
                Timber.w("No image quality output")
                resetFaceQuality()
                return@addOnSuccessListener
            }
            selfieQualityHistory.add(output)
            if (selfieQualityHistory.size > HISTORY_LENGTH) {
                // We should only ever exceed history length by 1
                selfieQualityHistory.removeAt(0)
            }

            val averageFaceQuality = selfieQualityHistory.average()
            // Timber.v("FaceQuality=$output")
            // Timber.v("AveragedFaceQuality=$averageFaceQuality")
            // Timber.v("ModelInferenceTime=$modelElapsedTimeMs ms")

            if (averageFaceQuality < FACE_QUALITY_THRESHOLD) {
                // We don't want to reset the history here, since the model output is noisy
                Timber.d("Face quality not met ($averageFaceQuality)")
                _uiState.update { it.copy(showBorderHighlight = false, cutoutOpacity = 0.8f) }
                return@addOnSuccessListener
            }
            _uiState.update {
                it.copy(showBorderHighlight = true, cutoutOpacity = 0f, selfieHint = null)
            }
            lastAutoCaptureTimeMs = System.currentTimeMillis()
            if (livenessFiles.size < NUM_LIVENESS_IMAGES) {
                val livenessFile = createLivenessFile()
                Timber.v("Capturing liveness image to $livenessFile")
                postProcessImageBitmap(
                    bitmap = fullSelfieBmp,
                    file = livenessFile,
                    saveAsGrayscale = false,
                    compressionQuality = 80,
                    maxOutputSize = LIVENESS_IMAGE_SIZE,
                )
                livenessFiles.add(livenessFile)
                return@addOnSuccessListener
            }
            shouldAnalyzeImages = false

            selfieFile = createSelfieFile()
            Timber.v("Capturing selfie image to $selfieFile")
            postProcessImageBitmap(
                bitmap = fullSelfieBmp,
                file = selfieFile!!,
                saveAsGrayscale = false,
                compressionQuality = 80,
                maxOutputSize = SELFIE_IMAGE_SIZE,
            )

            // When showCompletion is true, stop the camera feed and show the completion animation
            // TODO: show error on dialog itself?
            val proxy = { e: Throwable -> onResult(SmileIDResult.Error(e)) }
            viewModelScope.launch(getExceptionHandler(proxy)) {
                _uiState.update {
                    it.copy(
                        showLoading = true,
                        backgroundOpacity = 0.99f,
                        showBorderHighlight = false,
                    )
                }
                val result = submitJob()
                _uiState.update { it.copy(showLoading = false, showCompletion = true) }
                delay(2500)
                onResult(SmileIDResult.Success(result))
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error detecting faces")
            onResult(SmileIDResult.Error(exception))
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private suspend fun submitJob(): SmartSelfieJobResult.Entry {
        val authResponse = SmileID.api.authenticate(
            AuthenticationRequest(
                jobType = JobType.SmartSelfieAuthentication,
                enrollment = false,
                userId = userId,
                jobId = jobId,
            ),
        )
        return SmileID.api.doBiometricAuthentication(
            timestamp = authResponse.timestamp,
            signature = authResponse.signature,
            partnerParams = authResponse.partnerParams.copy(extras = extraPartnerParams),
            selfieImage = selfieFile!!.asFormDataPart("image", "image/jpeg"),
            livenessImages = livenessFiles.asFormDataParts("liveness_sequence", "image/jpeg"),
        )
    }

    private fun resetFaceQuality() {
        _uiState.update {
            it.copy(
                showBorderHighlight = false,
                cutoutOpacity = 0.8f,
                selfieHint = SelfieHint.SearchingForFace,
            )
        }
        selfieQualityHistory.clear()
        livenessFiles.removeAll { it.delete() }
        selfieFile?.delete()
        selfieFile = null
    }
}
