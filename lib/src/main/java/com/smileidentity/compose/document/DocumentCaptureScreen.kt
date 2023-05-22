package com.smileidentity.compose.document

import android.graphics.BitmapFactory
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.ImageCaptureConfirmationDialog
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview
import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.viewmodel.DocumentViewModel
import com.smileidentity.viewmodel.viewModelFactory
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ImageAnalysisBackpressureStrategy
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer
import com.ujizin.camposer.state.rememberTorch
import kotlinx.coroutines.delay
import java.io.File

/**
 * Orchestrates the document capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun OrchestratedDocumentCaptureScreen(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowGalleryUpload: Boolean = false,
    enforcedIdType: Document? = null,
    idAspectRatio: Float? = enforcedIdType?.aspectRatio,
    bypassSelfieCaptureWithFile: File? = null,
    titleText: String = "", // TODO - We need to pass this based on logic needed to capture 2 sides
    subtitleText: String = "",
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory {
            DocumentViewModel(
                userId = userId,
                jobId = jobId,
                enforcedIdType = enforcedIdType,
                idAspectRatio = idAspectRatio,
            )
        },
    ),
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }
    when {
        !acknowledgedInstructions -> DocumentCaptureInstructionsScreen(showAttribution) {
            acknowledgedInstructions = true
        }

        uiState.documentImageToConfirm != null -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.documentImageToConfirm.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_confirm_button),
            onConfirm = { viewModel.submitJob() },
            retakeButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_retake_button),
            onRetake = { viewModel.onDocumentRejected() },
        )

        else -> DocumentCaptureScreen(
            userId = userId,
            jobId = jobId,
            enforcedIdType = enforcedIdType,
            idAspectRatio = idAspectRatio,
            titleText = stringResource(id = R.string.si_doc_v_capture_instructions_front_title),
            subtitleText = stringResource(id = R.string.si_doc_v_capture_instructions_subtitle),
        )
    }
}

@VisibleForTesting
@Composable
internal fun DocumentCaptureScreen(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    enforcedIdType: Document? = null,
    idAspectRatio: Float? = enforcedIdType?.aspectRatio,
    titleText: String,
    subtitleText: String,
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory {
            DocumentViewModel(
                userId = userId,
                jobId = jobId,
                enforcedIdType = enforcedIdType,
                idAspectRatio = idAspectRatio,
            )
        },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Back)
    var torchEnabled by cameraState.rememberTorch(initialTorch = true)
    var isFocusing by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                uiState.currentBoundingBox?.let {
                    val scaleFactor = size.width / uiState.fullImageWidth
                    val composeRect = Rect(
                        left = it.left * scaleFactor,
                        top = it.top * scaleFactor,
                        right = it.right * scaleFactor,
                        bottom = it.bottom * scaleFactor,
                    )
                    it.toComposeRect()
                    drawRect(
                        color = Color.Red,
                        topLeft = composeRect.topLeft,
                        size = composeRect.size,
                    )
                }
            },
    ) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            scaleType = ScaleType.FillCenter,
            zoomRatio = 1.0f,
            enableTorch = torchEnabled,
            onFocus = { onComplete ->
                isFocusing = true
                delay(1000L)
                isFocusing = false
                onComplete()
            },
            imageAnalyzer = cameraState.rememberImageAnalyzer(
                analyze = viewModel::analyzeImage,
                // Guarantees only one image will be delivered for analysis at a time
                imageAnalysisBackpressureStrategy = ImageAnalysisBackpressureStrategy.KeepOnlyLatest,
            ),
            modifier = Modifier
                .testTag("document_camera_preview")
                .fillMaxSize()
                .clipToBounds(),
        )
        uiState.currentBoundingBox?.let {
            val topLeft = Offset(it.left.toFloat(), it.top.toFloat())
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Green,
                    style = Stroke(width = 4f),
                    topLeft = topLeft,
                    size = Size(it.width().toFloat(), it.height().toFloat()),
                )
            }
        }
        DocumentShapedProgressIndicator(
            isDocumentDetected = uiState.isDocumentDetected,
            modifier = Modifier
                .fillMaxSize()
                .testTag("selfie_progress_indicator"),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                CaptureDocumentButton(enabled = !isFocusing) {
                    viewModel.takeButtonCaptureDocument(cameraState = cameraState)
                }
                // todo: flash icon
                Image(
                    painter = painterResource(id = R.drawable.si_camera_capture),
                    contentDescription = "smile_camera_torch",
                    modifier = Modifier
                        .size(70.dp)
                        .clickable {
                            torchEnabled = !torchEnabled
                        },
                )
            }
        }
    }
}

@Composable
private fun CaptureDocumentButton(
    enabled: Boolean,
    onCaptureClicked: () -> Unit,
) {
    IconButton(
        onClick = onCaptureClicked,
        enabled = enabled,
        modifier = Modifier.size(70.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.si_camera_capture),
            contentDescription = "Document Capture Button",
            // tint = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
        )
    }

    // Image(
    //     painter = painterResource(id = R.drawable.si_camera_capture),
    //     contentDescription = "Document Capture Button",
    //     alpha = if (enabled) 1f else 0.5f,
    //     modifier = Modifier
    //         .size(70.dp)
    //         .clickable(enabled) { onCaptureClicked.invoke() }
    // )
}

@SmilePreview
@Composable
private fun DocumentCaptureScreenPreview() {
    Preview {
        DocumentCaptureScreen(
            titleText = "Front of National ID Card",
            subtitleText = "Make sure all corners are visible and there is no glare",
        )
    }
}
