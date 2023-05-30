package com.smileidentity.compose.document

import android.graphics.BitmapFactory
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
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
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
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
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            scaleType = ScaleType.FillCenter,
            modifier = Modifier
                .testTag("document_camera_preview")
                .fillMaxSize()
                .clipToBounds(),
        )
        DocumentShapedBoundingBox(
            aspectRatio = idAspectRatio,
            modifier = Modifier
                .fillMaxSize()
                .testTag("document_progress_indicator"),
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
            CaptureDocumentButton { viewModel.takeButtonCaptureDocument(cameraState = cameraState) }
        }
    }
}

@Composable
private fun CaptureDocumentButton(
    onCaptureClicked: () -> Unit,
) {
    Image(
        painter = painterResource(id = R.drawable.si_camera_capture),
        contentDescription = "smile_camera_capture",
        modifier = Modifier
            .size(70.dp)
            .clickable { onCaptureClicked.invoke() },
    )
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
