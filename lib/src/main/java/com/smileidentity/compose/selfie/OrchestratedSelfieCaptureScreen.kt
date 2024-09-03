package com.smileidentity.compose.selfie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smileidentity.R
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.nav.ImageConfirmParams
import com.smileidentity.compose.nav.InstructionScreenParams
import com.smileidentity.compose.nav.ProcessingScreenParams
import com.smileidentity.compose.nav.ResultCallbacks
import com.smileidentity.compose.nav.Routes
import com.smileidentity.compose.nav.SelfieCaptureParams
import com.smileidentity.compose.nav.encodeUrl
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.SelfieViewModel
import com.smileidentity.viewmodel.viewModelFactory
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * Orchestrates the selfie capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun OrchestratedSelfieCaptureScreen(
    childNavController: NavController,
    resultCallbacks: ResultCallbacks,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = false,
    skipApiSubmission: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
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
                extraPartnerParams = extraPartnerParams,
            )
        },
    ),
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        content()
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }
    resultCallbacks.onSmartSelfieResult = onResult
    resultCallbacks.onConfirmCapturedImage = viewModel::submitJob
    resultCallbacks.onImageDialogRetake = viewModel::onSelfieRejected
    resultCallbacks.onSelfieInstructionScreen = {
        acknowledgedInstructions = true
        resultCallbacks.selfieViewModel = viewModel
        childNavController.navigate(
            Routes.Selfie.CaptureScreen(
                SelfieCaptureParams(
                    userId = userId,
                    jobId = jobId,
                    isEnroll = isEnroll,
                    allowAgentMode = allowAgentMode,
                    skipApiSubmission = skipApiSubmission,
                    showAttribution = showAttribution,
                    extraPartnerParams = extraPartnerParams,
                    showInstructions = showInstructions,
                ),
            ),
        )
    }
    when {
        showInstructions && !acknowledgedInstructions -> {
            childNavController.navigate(
                Routes.Selfie.InstructionsScreen(
                    InstructionScreenParams(showAttribution),
                ),
            )
        }

        uiState.processingState != null -> {
            childNavController.navigate(
                Routes.Shared.ProcessingScreen(
                    ProcessingScreenParams(
                        processingState = uiState.processingState,
                        inProgressTitle = R.string.si_smart_selfie_processing_title,
                        inProgressSubtitle = R.string.si_smart_selfie_processing_subtitle,
                        inProgressIcon = R.drawable.si_smart_selfie_processing_hero,
                        successTitle = R.string.si_smart_selfie_processing_success_title,
                        successSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                            ?: stringResource(R.string.si_smart_selfie_processing_success_subtitle),
                        successIcon = R.drawable.si_processing_success,
                        errorTitle = R.string.si_smart_selfie_processing_error_title,
                        errorSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                            ?: stringResource(id = R.string.si_processing_error_subtitle),
                        errorIcon = R.drawable.si_processing_error,
                        continueButtonText = R.string.si_continue,
                        retryButtonText = R.string.si_smart_selfie_processing_retry_button,
                        closeButtonText = R.string.si_smart_selfie_processing_close_button,
                    ),
                ),
            )
        }

        uiState.selfieToConfirm != null -> {
            childNavController.navigate(
                Routes.Shared.ImageConfirmDialog(
                    ImageConfirmParams(
                        titleText = R.string.si_smart_selfie_confirmation_dialog_title,
                        subtitleText = R.string.si_smart_selfie_confirmation_dialog_subtitle,
                        imageFilePath = encodeUrl(uiState.selfieToConfirm.absolutePath),
                        confirmButtonText =
                        R.string.si_smart_selfie_confirmation_dialog_confirm_button,
                        retakeButtonText =
                        R.string.si_smart_selfie_confirmation_dialog_retake_button,
                        scaleFactor = 1.0f,
                    ),
                ),
            )
        }
    }
}
