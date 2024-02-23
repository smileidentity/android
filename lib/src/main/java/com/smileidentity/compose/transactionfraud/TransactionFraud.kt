package com.smileidentity.compose.transactionfraud

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.OperationCanceledException
import androidx.annotation.IntRange
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.smileidentity.ml.ImQualCp20Optimized
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.rotated
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import timber.log.Timber

const val HISTORY_LENGTH = 10
const val FACE_QUALITY_THRESHOLD = 50

@kotlin.OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TransactionFraudScreen(
    modifier: Modifier = Modifier,
    onResult: SmileIDCallback<Nothing> = {},
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        // TODO: Handle denied state
    }
    // TODO: Request Permissions if not granted
    Dialog(
        onDismissRequest = {
            onResult(SmileIDResult.Error(OperationCanceledException("User Cancelled")))
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
    ) {
        // TODO: Fix the Context passing
        TransactionFraudScreen(
            context = context,
            onResult = onResult,
            modifier = modifier
                .height(512.dp)
                .clip(MaterialTheme.shapes.large),
        )
    }
}

@Composable
private fun TransactionFraudScreen(
    context: Context,
    modifier: Modifier = Modifier,
    onResult: SmileIDCallback<Nothing> = {},
    viewModel: TransactionFraudViewModel = viewModel(
        initializer = { TransactionFraudViewModel(context) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Front)
    Box(contentAlignment = BottomCenter, modifier = modifier) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            implementationMode = ImplementationMode.Compatible,
            scaleType = ScaleType.FillCenter,
            imageAnalyzer = cameraState.rememberImageAnalyzer(analyze = viewModel::analyzeImage),
            isImageAnalysisEnabled = true,
            modifier = Modifier.fillMaxSize(),
        )
        // Overlay
        // When conditions are *not* met:
        // - Show white corners
        // - Show extra dimmed overlay on top

        // When conditions *are* met:
        // - show orange corners
        // - transition to circle (under what condition?)


        // Overlay shows white corners when face is not detected/conditions are not met
        // Should switch to orange corners when conditions *are* met


        val faceQualityTextColor = if (uiState.faceQuality > FACE_QUALITY_THRESHOLD) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.error
        }
        val avgFaceQualityTextColor = if (uiState.averagedFaceQuality > FACE_QUALITY_THRESHOLD) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.error
        }
        Column(
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 64.dp),
        ) {
            Text(
                text = "Face Quality: ${uiState.faceQuality}",
                textAlign = TextAlign.Center,
                color = animateColorAsState(
                    targetValue = faceQualityTextColor,
                    label = "faceQualityText",
                ).value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Avg ($HISTORY_LENGTH frames): ${uiState.averagedFaceQuality}",
                textAlign = TextAlign.Center,
                color = animateColorAsState(
                    targetValue = avgFaceQualityTextColor,
                    label = "avgFaceQualityText",
                ).value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * This component serves as an overlay over the main Camera UI component. It takes in various
 * parameters related to the state of detection and provides a purely visual feedback in the overlay
 *
 * The overlay and feedback is dynamic/there may be multiple states. For example, we may have
 * a square cutout or circle. the corner borders may be white or orange.
 *
 * There may be animations overlaid
 */
@Composable
private fun FeedbackOverlay(
    backgroundOpacity: Float,
    cutoutOpacity: Float,
    // cutoutShape: Shape,
    hintAnimation: Painter, // TODO: Make this a Lottie animation
    // border: BorderStroke,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val path = Path().apply {
            // addRoundRect(RoundRect())
        }
        // clipPath()
    }
}

data class TransactionFraudUiState(
    @IntRange(0, 100) val faceQuality: Int = 0,
    @IntRange(0, 100) val averagedFaceQuality: Int = 0,
)

@kotlin.OptIn(FlowPreview::class)
class TransactionFraudViewModel(context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionFraudUiState())
    val uiState = _uiState.asStateFlow().sample(250).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        TransactionFraudUiState(),
    )
    private val modelInputSize = intArrayOf(1, 120, 120, 3)
    private val faceDetectorOptions = FaceDetectorOptions.Builder().apply {
        setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
    }.build()

    private val faceDetector by lazy { FaceDetection.getClient(faceDetectorOptions) }
    private val imageQualityModel = ImQualCp20Optimized.newInstance(context)
    private val selfieQualityHistory = mutableListOf<Int>()

    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: run {
            Timber.w("ImageProxy has no image")
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            // TODO: Add all the protections
            val face = faces.firstOrNull() ?: run {
                Timber.w("No face detected")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            val bBox = face.boundingBox

            // Check that the corners of the face bounding box are within the inputImage
            val faceCornersInImage = bBox.left >= 0 && bBox.right <= inputImage.width &&
                bBox.top >= 0 && bBox.bottom <= inputImage.height
            if (!faceCornersInImage) {
                Timber.w("Face bounding box not within image")
                resetFaceQuality()
                return@addOnSuccessListener
            }

            // face mesh returns 480ish points. take min/max of all those points. use that as
            // bounding box
            // Check that the corners of the face bounding box are within the inputImage

            // returns a matrix, each row is a probability of being a quality
            // get 1 row if batch size is 1
            // 1st column is the actual quality
            // theoretically, 2nd column is 1-(1st_column)

            // model is trained on *face mesh* crop (different from face detection potentially)

            val startTime = System.nanoTime()
            val bitmap = with(imageProxy.toBitmap().rotated(imageProxy.imageInfo.rotationDegrees)) {
                if (bBox.left + bBox.width() > this.width) {
                    Timber.w("Face bounding box width is greater than image width")
                    _uiState.update { it.copy(faceQuality = 0) }
                    return@addOnSuccessListener
                }

                if (bBox.top + bBox.height() > this.height) {
                    Timber.w("Face bounding box height is greater than image height")
                    _uiState.update { it.copy(faceQuality = 0) }
                    return@addOnSuccessListener
                }

                val croppedBitmap = Bitmap.createBitmap(
                    this,
                    bBox.left,
                    bBox.top,
                    bBox.width(),
                    bBox.height(),
                    // NB! bBox is not guaranteed to be square, so scale might squish the image
                ).scale(modelInputSize[1], modelInputSize[2], false)
                recycle()
                return@with croppedBitmap
            }

            // Image Quality Model Inference
            val input = TensorImage(DataType.FLOAT32).apply { load(bitmap) }
            val outputs = imageQualityModel.process(input.tensorBuffer)
            val output = outputs.outputFeature0AsTensorBuffer.floatArray.firstOrNull() ?: run {
                Timber.e("No image quality output")
                return@addOnSuccessListener
            }
            val displayedOutput = (output * 100).toInt()
            selfieQualityHistory.add(displayedOutput)
            if (selfieQualityHistory.size > HISTORY_LENGTH) {
                selfieQualityHistory.removeAt(0)
            }

            val elapsedTimeMs = (System.nanoTime() - startTime) / 1_000_000
            Timber.d("Face Quality: $output (model inference time: $elapsedTimeMs ms)")

            _uiState.update {
                it.copy(
                    faceQuality = displayedOutput,
                    averagedFaceQuality = selfieQualityHistory.average().toInt(),
                )
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Error detecting faces")
            resetFaceQuality()
        }.addOnCompleteListener {
            // Closing the proxy allows the next image to be delivered to the analyzer
            imageProxy.close()
        }
    }

    private fun resetFaceQuality() {
        selfieQualityHistory.clear()
        _uiState.update { it.copy(faceQuality = 0, averagedFaceQuality = 0) }
    }
}
