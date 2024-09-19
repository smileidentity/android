package com.smileidentity.compose.selfie

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.ForceBrightness
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.MAX_FACE_AREA_THRESHOLD
import com.smileidentity.viewmodel.SelfieViewModel
import com.smileidentity.viewmodel.viewModelFactory
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.ImageAnalysisBackpressureStrategy.KeepOnlyLatest
import com.ujizin.camposer.state.ImplementationMode
import com.ujizin.camposer.state.ScaleType
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import com.ujizin.camposer.state.rememberImageAnalyzer

/**
 * The actual selfie capture screen, which shows the camera preview and the progress indicator
 */
@Composable
fun SelfieCaptureScreen(
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = true,
    skipApiSubmission: Boolean = false,
    metadata: SnapshotStateList<Metadatum> = LocalMetadata.current,
    viewModel: SelfieViewModel = viewModel(
        factory = viewModelFactory {
            SelfieViewModel(
                isEnroll = isEnroll,
                userId = userId,
                jobId = jobId,
                allowNewEnroll = allowNewEnroll,
                skipApiSubmission = skipApiSubmission,
                metadata = metadata,
            )
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraState = rememberCameraState()
    var camSelector by rememberCamSelector(CamSelector.Front)
    val viewfinderZoom = 1.1f
    val faceFillPercent = remember { MAX_FACE_AREA_THRESHOLD * viewfinderZoom * 2 }
    // Force maximum brightness in order to light up the user's face
    ForceBrightness()
    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            implementationMode = ImplementationMode.Performance,
            imageAnalyzer = cameraState.rememberImageAnalyzer(
                analyze = { viewModel.analyzeImage(it, camSelector) },
                // Guarantees only one image will be delivered for analysis at a time
                imageAnalysisBackpressureStrategy = KeepOnlyLatest,
            ),
            isImageAnalysisEnabled = true,
            scaleType = ScaleType.FillCenter,
            zoomRatio = 1.0f,
            modifier = Modifier
                .testTag("selfie_camera_preview")
                .fillMaxSize()
                .clipToBounds()
                // Scales the *preview* WITHOUT changing the zoom ratio, to allow capture of
                // "out of bounds" content as a fraud prevention technique
                .scale(viewfinderZoom),
        )
        val animatedProgress by animateFloatAsState(
            targetValue = uiState.progress,
            animationSpec = tween(easing = LinearEasing),
            label = "selfie_progress",
        )
        FaceShapedProgressIndicator(
            progress = animatedProgress,
            faceFillPercent = faceFillPercent,
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
                text = stringResource(uiState.directive.displayText),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            if (allowAgentMode) {
                AgentModeSwitch(
                    isAgentModeEnabled = camSelector == CamSelector.Back,
                    onCamSelectorChange = { camSelector = camSelector.inverse },
                )
            }
        }
    }
}

@Composable
internal fun AgentModeSwitch(isAgentModeEnabled: Boolean, onCamSelectorChange: (Boolean) -> Unit) {
    val agentModeBackgroundColor = if (isAgentModeEnabled) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(32.dp))
            .background(agentModeBackgroundColor)
            .padding(8.dp, 0.dp),
    ) {
        Text(
            text = stringResource(R.string.si_agent_mode),
            color = contentColorFor(agentModeBackgroundColor),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(4.dp, 0.dp),
        )
        Switch(
            checked = isAgentModeEnabled,
            onCheckedChange = onCamSelectorChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.tertiary,
            ),
            modifier = Modifier.testTag("agent_mode_switch"),
        )
    }
}

@SmilePreviews
@Composable
private fun SelfieCaptureScreenPreview() {
    Preview {
        SelfieCaptureScreen(allowAgentMode = true)
    }
}
