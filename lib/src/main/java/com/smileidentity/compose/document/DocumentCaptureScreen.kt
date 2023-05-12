package com.smileidentity.compose.document

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview
import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.viewmodel.DocumentViewModel
import com.smileidentity.viewmodel.MAX_FACE_AREA_THRESHOLD
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
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory { DocumentViewModel() },
    ),
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    DocumentCaptureScreen()
}

@VisibleForTesting
@Composable
internal fun DocumentCaptureScreen(
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory { DocumentViewModel() },
    ),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Back)
    val viewfinderZoom = 1.1f
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            scaleType = ScaleType.FillCenter,
            zoomRatio = 1.0f,
            modifier = Modifier
                .testTag("document_camera_preview")
                .fillMaxSize()
                .clipToBounds(),
        )
        DocumentShapedProgressIndicator(
            documentFillPercent = MAX_FACE_AREA_THRESHOLD * viewfinderZoom * 2,
            modifier = Modifier
                .fillMaxSize()
                .testTag("selfie_progress_indicator"),
        )
        Image(
            painter = painterResource(id = R.drawable.si_camera_capture),
            contentDescription = "smile_camera_capture",
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.Center)
                .clickable { viewModel.takeButtonCaptureDocument(cameraState = cameraState) },
        )
    }
}

@SmilePreview
@Composable
private fun DocumentCaptureScreenPreview() {
    Preview {
        DocumentCaptureScreen()
    }
}
