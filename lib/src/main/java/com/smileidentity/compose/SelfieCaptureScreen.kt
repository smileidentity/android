package com.smileidentity.compose

import android.graphics.BitmapFactory
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import com.smileidentity.randomUserId
import com.smileidentity.results.SmartSelfieResult
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
 * Orchestrates the selfie capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun OrchestratedSelfieCaptureScreen(
    userId: String = randomUserId(),
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    viewModel: SelfieViewModel = viewModel(
        factory = viewModelFactory { SelfieViewModel(isEnroll, userId) },
    ),
    onResult: SmartSelfieResult.Callback = SmartSelfieResult.Callback {},
) {
    val uiState = viewModel.uiState.collectAsState().value
    var acknowledgedInstructions by remember { mutableStateOf(false) }
    when {
        !acknowledgedInstructions -> SmartSelfieInstructionsScreen(showAttribution) {
            acknowledgedInstructions = true
        }

        uiState.processingState != null -> ProcessingScreen(
            processingState = uiState.processingState,
            inProgressTitle = stringResource(R.string.si_smart_selfie_processing_title),
            inProgressSubtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
            inProgressIcon = painterResource(R.drawable.si_smart_selfie_processing_hero),
            successTitle = stringResource(R.string.si_smart_selfie_processing_success_title),
            successSubtitle = stringResource(R.string.si_smart_selfie_processing_success_subtitle),
            successIcon = painterResource(R.drawable.si_processing_success),
            errorTitle = stringResource(R.string.si_smart_selfie_processing_error_title),
            errorSubtitle = stringResource(
                uiState.errorMessage ?: R.string.si_smart_selfie_processing_error_subtitle,
            ),
            errorIcon = painterResource(R.drawable.si_processing_error),
            continueButtonText = stringResource(R.string.si_smart_selfie_processing_continue_button),
            onContinue = { viewModel.onFinished(onResult) },
            retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
            onRetry = { viewModel.onRetry() },
            closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
            onClose = { viewModel.onFinished(onResult) },
        )

        uiState.selfieToConfirm != null -> ImageCaptureConfirmationDialog(
            titleText = stringResource(R.string.si_smart_selfie_confirmation_dialog_title),
            subtitleText = stringResource(R.string.si_smart_selfie_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.selfieToConfirm.absolutePath).asImageBitmap(),
            ),
            confirmButtonText = stringResource(R.string.si_smart_selfie_confirmation_dialog_confirm_button),
            onConfirm = { viewModel.submitJob() },
            retakeButtonText = stringResource(R.string.si_smart_selfie_confirmation_dialog_retake_button),
            onRetake = { viewModel.onSelfieRejected() },
        )

        else -> SelfieCaptureScreen(
            userId = userId,
            isEnroll = isEnroll,
            allowAgentMode = allowAgentMode,
        )
    }
}

@VisibleForTesting
@Composable
internal fun SelfieCaptureScreen(
    userId: String = randomUserId(),
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = true,
    viewModel: SelfieViewModel = viewModel(
        factory = viewModelFactory { SelfieViewModel(isEnroll, userId) },
    ),
) {
    val uiState = viewModel.uiState.collectAsState().value
    val cameraState = rememberCameraState()
    var camSelector by rememberCamSelector(CamSelector.Front)
    val viewfinderZoom = 1.1f
    // Force maximum brightness in order to light up the user's face
    ForceBrightness()
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            cameraState = cameraState,
            camSelector = camSelector,
            implementationMode = ImplementationMode.Performance,
            imageAnalyzer = cameraState.rememberImageAnalyzer(
                analyze = viewModel::analyzeImage,
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
        val animatedProgress = animateFloatAsState(
            targetValue = uiState.progress,
            animationSpec = tween(),
        ).value
        FaceShapedProgressIndicator(
            progress = animatedProgress,
            faceFillPercent = MAX_FACE_AREA_THRESHOLD * viewfinderZoom * 2,
            modifier = Modifier.fillMaxSize().testTag("selfie_progress_indicator"),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            Text(
                text = stringResource(uiState.currentDirective.displayText),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AgentModeSwitch(
                allowAgentMode = allowAgentMode,
                camSelector = camSelector,
                onCamSelectorChange = { camSelector = camSelector.inverse },
            )
        }
    }
}

@Composable
private fun AgentModeSwitch(
    allowAgentMode: Boolean,
    camSelector: CamSelector,
    onCamSelectorChange: (Boolean) -> Unit,
) {
    if (allowAgentMode) {
        val isAgentModeEnabled = camSelector == CamSelector.Back
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
}

@Preview
@Composable
private fun SelfieCaptureScreenPreview() {
    MaterialTheme(colorScheme = SmileID.colorScheme, typography = SmileID.typography) {
        SelfieCaptureScreen(allowAgentMode = true)
    }
}
