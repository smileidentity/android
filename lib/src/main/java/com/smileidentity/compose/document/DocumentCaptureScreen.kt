package com.smileidentity.compose.document

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreview
import com.smileidentity.randomUserId
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.viewmodel.DocumentViewModel
import com.smileidentity.viewmodel.MAX_FACE_AREA_THRESHOLD
import com.smileidentity.viewmodel.viewModelFactory
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState

/**
 * Orchestrates the document capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun OrchestratedDocumentCaptureScreen(
    userId: String = rememberSaveable { randomUserId() },
    showAttribution: Boolean = true,
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory { DocumentViewModel() },
    ),
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {}, // TODO - Fix me
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
                .clipToBounds()
                // Scales the *preview* WITHOUT changing the zoom ratio, to allow capture of
                // "out of bounds" content as a fraud prevention technique
                .scale(viewfinderZoom),
        )
        val animatedProgress = animateFloatAsState(
            targetValue = uiState.progress,
            animationSpec = tween(),
            label = "document_progress",
        ).value
        DocumentShapedProgressIndicator(
            progress = animatedProgress,
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
                .clickable { viewModel.captureDocument(cameraState = cameraState) },
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
