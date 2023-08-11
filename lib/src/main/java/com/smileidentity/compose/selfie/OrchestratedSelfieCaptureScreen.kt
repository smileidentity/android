package com.smileidentity.compose.selfie

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.ImageCaptureConfirmationDialog
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.SelfieViewModel
import com.smileidentity.viewmodel.viewModelFactory

/**
 * Orchestrates the selfie capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun OrchestratedSelfieCaptureScreen(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = false,
    skipApiSubmission: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    viewModel: SelfieViewModel = viewModel(
        factory = viewModelFactory { SelfieViewModel(isEnroll, userId, jobId, skipApiSubmission) },
    ),
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }
    when {
        showInstructions && !acknowledgedInstructions -> SmartSelfieInstructionsScreen(
            showAttribution = showAttribution,
        ) {
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
                uiState.errorMessage ?: R.string.si_processing_error_subtitle,
            ),
            errorIcon = painterResource(R.drawable.si_processing_error),
            continueButtonText = stringResource(R.string.si_continue),
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
            confirmButtonText = stringResource(
                R.string.si_smart_selfie_confirmation_dialog_confirm_button,
            ),
            onConfirm = { viewModel.submitJob() },
            retakeButtonText = stringResource(
                R.string.si_smart_selfie_confirmation_dialog_retake_button,
            ),
            onRetake = { viewModel.onSelfieRejected() },
            scaleFactor = 1.25f,
        )

        else -> SelfieCaptureScreen(
            userId = userId,
            jobId = jobId,
            isEnroll = isEnroll,
            allowAgentMode = allowAgentMode,
            skipApiSubmission = skipApiSubmission,
        )
    }
}
