package com.smileidentity.compose.document

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.ImageCaptureConfirmationDialog
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.util.generateFileFromUri
import com.smileidentity.util.isValidDocumentImage
import com.smileidentity.util.toast
import com.smileidentity.viewmodel.document.DocumentCaptureViewModel
import com.smileidentity.viewmodel.viewModelFactory
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageAnalysisBackpressureStrategy.KeepOnlyLatest
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer
import timber.log.Timber
import java.io.File

internal enum class DocumentCaptureSide {
    Front,
    Back,
}

/**
 * This handles Instructions + Capture + Confirmation for a single side of a document
 */
@Composable
internal fun DocumentCaptureScreen(
    side: DocumentCaptureSide,
    showInstructions: Boolean,
    showAttribution: Boolean,
    allowGallerySelection: Boolean,
    instructionsTitleText: String,
    instructionsSubtitleText: String,
    captureTitleText: String,
    idAspectRatio: Float?,
    onConfirm: (File) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocumentCaptureViewModel = viewModel(
        factory = viewModelFactory { DocumentCaptureViewModel() },
        key = side.name,
    ),
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Timber.v("selectedUri: $uri")
            if (uri == null) {
                Timber.e("selectedUri is null")
                context.toast(R.string.si_doc_v_capture_error_subtitle)
                return@rememberLauncherForActivityResult
            }
            if (isValidDocumentImage(context, uri)) {
                val selectedPhotoFile = generateFileFromUri(uri = uri, context = context)
                viewModel.onPhotoSelectedFromGallery(selectedPhotoFile)
            } else {
                context.toast(R.string.si_doc_v_validation_image_too_small)
            }
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val documentImageToConfirm = uiState.documentImageToConfirm
    val captureError = uiState.captureError
    when {
        captureError != null -> onError(captureError)
        showInstructions && !uiState.acknowledgedInstructions -> {
            DocumentCaptureInstructionsScreen(
                title = instructionsTitleText,
                subtitle = instructionsSubtitleText,
                showAttribution = showAttribution,
                allowPhotoFromGallery = allowGallerySelection,
                onInstructionsAcknowledgedSelectFromGallery = {
                    Timber.v("onInstructionsAcknowledgedSelectFromGallery")
                    photoPickerLauncher.launch(PickVisualMediaRequest(ImageOnly))
                },
                onInstructionsAcknowledgedTakePhoto = {
                    viewModel.onInstructionsAcknowledged()
                },
            )
        }

        documentImageToConfirm != null -> {
            val painter = remember {
                BitmapPainter(
                    BitmapFactory.decodeFile(documentImageToConfirm.absolutePath).asImageBitmap(),
                )
            }
            ImageCaptureConfirmationDialog(
                titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
                subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
                painter = painter,
                confirmButtonText = stringResource(
                    id = R.string.si_doc_v_confirmation_dialog_confirm_button,
                ),
                onConfirm = { onConfirm(documentImageToConfirm) },
                retakeButtonText = stringResource(
                    id = R.string.si_doc_v_confirmation_dialog_retake_button,
                ),
                onRetake = viewModel::onRetry,
            )
        }

        else -> CaptureScreenContent(
            titleText = captureTitleText,
            subtitleText = stringResource(id = uiState.directive.displayText),
            idAspectRatio = idAspectRatio,
            areEdgesDetected = uiState.areEdgesDetected,
            showCaptureInProgress = uiState.showCaptureInProgress,
            showManualCaptureButton = uiState.showManualCaptureButton,
            onCaptureClicked = viewModel::captureDocument,
            imageAnalyzer = viewModel,
            modifier = modifier,
        )
    }
}

@Composable
private fun CaptureScreenContent(
    titleText: String,
    subtitleText: String,
    idAspectRatio: Float?,
    areEdgesDetected: Boolean,
    showCaptureInProgress: Boolean,
    showManualCaptureButton: Boolean,
    onCaptureClicked: (CameraState) -> Unit,
    imageAnalyzer: ImageAnalysis.Analyzer,
    modifier: Modifier = Modifier,
) {
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Back)
    Column(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            CameraPreview(
                cameraState = cameraState,
                camSelector = camSelector,
                scaleType = ScaleType.FillCenter,
                isImageAnalysisEnabled = true,
                imageAnalyzer = cameraState.rememberImageAnalyzer(
                    analyze = imageAnalyzer,
                    // Guarantees only one image will be delivered for analysis at a time
                    imageAnalysisBackpressureStrategy = KeepOnlyLatest,
                ),
                modifier = Modifier
                    .testTag("document_camera_preview")
                    .fillMaxSize()
                    .clipToBounds(),
            )
            DocumentShapedBoundingBox(
                aspectRatio = idAspectRatio,
                areEdgesDetected = areEdgesDetected,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .consumeWindowInsets(WindowInsets.safeDrawing)
                    .fillMaxSize()
                    .testTag("document_progress_indicator"),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight()
                .fillMaxWidth(),
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                // Since some directives are longer, setting minLines to 2 reserves space in the
                // layout to prevent UI elements from moving around when the directive changes
                minLines = 2,
                modifier = Modifier.padding(4.dp),
            )

            // By using Box with a fixed size here, we ensure the UI doesn't move around when the
            // manual capture button becomes visible
            Box(modifier = Modifier.size(64.dp)) {
                if (showCaptureInProgress) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize())
                } else if (showManualCaptureButton) {
                    CaptureDocumentButton { onCaptureClicked(cameraState) }
                }
            }
        }
    }
}

@Composable
private fun CaptureDocumentButton(
    modifier: Modifier = Modifier,
    onCaptureClicked: () -> Unit,
) {
    Image(
        painter = painterResource(id = R.drawable.si_camera_capture),
        contentDescription = "smile_camera_capture",
        modifier = modifier
            .clickable(onClick = onCaptureClicked),
    )
}

@SmilePreviews
@Composable
private fun CaptureScreenContentPreview() {
    Preview {
        CaptureScreenContent(
            titleText = "Front of National ID Card",
            subtitleText = "Make sure all corners are visible and there is no glare",
            idAspectRatio = 1.59f,
            areEdgesDetected = true,
            showCaptureInProgress = false,
            showManualCaptureButton = true,
            onCaptureClicked = {},
            imageAnalyzer = {},
        )
    }
}
