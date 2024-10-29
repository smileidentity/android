package com.smileidentity.compose.document

import androidx.camera.core.ImageProxy
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageAnalysisBackpressureStrategy.KeepOnlyLatest
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer

@Composable
fun CaptureScreenContent(
    titleText: String,
    subtitleText: String,
    idAspectRatio: Float,
    areEdgesDetected: Boolean,
    showCaptureInProgress: Boolean,
    showManualCaptureButton: Boolean,
    onCaptureClicked: (CameraState) -> Unit,
    imageAnalyzer: (ImageProxy, CameraState) -> Unit,
    onFocusEvent: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraState = rememberCameraState()
    val camSelector by rememberCamSelector(CamSelector.Back)
    val lifecycleOwner = LocalLifecycleOwner.current
    cameraState.controller.tapToFocusState.observe(lifecycleOwner, onFocusEvent)
    Column(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .clipToBounds()
                .fillMaxSize()
                .weight(1f),
        ) {
            CameraPreview(
                cameraState = cameraState,
                camSelector = camSelector,
                scaleType = ScaleType.FillCenter,
                isImageAnalysisEnabled = true,
                imageAnalyzer = cameraState.rememberImageAnalyzer(
                    analyze = { imageAnalyzer(it, cameraState) },
                    // Guarantees only one image will be delivered for analysis at a time
                    imageAnalysisBackpressureStrategy = KeepOnlyLatest,
                ),
                modifier = Modifier
                    .testTag("document_camera_preview")
                    .fillMaxSize()
                    .clipToBounds()
                    // Scales the *preview* WITHOUT changing the zoom ratio, to allow capture of
                    // "out of bounds" content as a fraud prevention technique AND UX reasons
                    .scale(PREVIEW_SCALE_FACTOR),
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
private fun CaptureDocumentButton(modifier: Modifier = Modifier, onCaptureClicked: () -> Unit) {
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
            imageAnalyzer = { _, _ -> },
            onFocusEvent = {},
        )
    }
}
