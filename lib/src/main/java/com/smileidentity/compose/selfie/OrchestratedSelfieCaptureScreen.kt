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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.SmileSelfieCaptureScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SmileSmartSelfieInstructionsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.smileidentity.compose.selfie.navigation.SelfieGraph
import com.smileidentity.compose.selfie.ui.SelfieCaptureScreen
import com.smileidentity.compose.selfie.ui.SmartSelfieInstructionsScreen
import com.smileidentity.compose.selfie.viewmodel.OrchestratedSelfieViewModel
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.smileViewModel

/**
 * Orchestrates the selfie capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Destination<SelfieGraph>(start = true)
@Composable
internal fun OrchestratedSelfieCaptureScreen(
    navigator: DestinationsNavigator,
    // extraPartnerParams: ImmutableMap<String, String>,
    // metadata: ImmutableList<Metadatum>,
    modifier: Modifier = Modifier,
    viewModel: OrchestratedSelfieViewModel = smileViewModel(),
    userId: String = randomUserId(),
    jobId: String = randomJobId(),
    allowNewEnroll: Boolean = false,
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    // var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        // todo - maybe make this a destination wrapper?
        navigator.navigate(
            direction = SmileSmartSelfieInstructionsScreenDestination(showAttribution = true),
        )

        // navigator.navigate(SmileSmartSelfieInstructionsScreenDestination)
        // when {
        //     // showInstructions && !acknowledgedInstructions -> SmartSelfieInstructionsScreen(
        //     //     showAttribution = showAttribution,
        //     // ) {
        //     //     acknowledgedInstructions = true
        //     // }
        //
        //     uiState.processingState != null -> ProcessingScreen(
        //         processingState = uiState.processingState,
        //         inProgressTitle = stringResource(R.string.si_smart_selfie_processing_title),
        //         inProgressSubtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
        //         inProgressIcon = painterResource(R.drawable.si_smart_selfie_processing_hero),
        //         successTitle = stringResource(R.string.si_smart_selfie_processing_success_title),
        //         successSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
        //             ?: stringResource(R.string.si_smart_selfie_processing_success_subtitle),
        //         successIcon = painterResource(R.drawable.si_processing_success),
        //         errorTitle = stringResource(R.string.si_smart_selfie_processing_error_title),
        //         errorSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
        //             ?: stringResource(id = R.string.si_processing_error_subtitle),
        //         errorIcon = painterResource(R.drawable.si_processing_error),
        //         continueButtonText = stringResource(R.string.si_continue),
        //         onContinue = { viewModel.onFinished(onResult) },
        //         retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
        //         onRetry = {},
        //         closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
        //         onClose = { viewModel.onFinished(onResult) },
        //     )
        //
        //     else -> SelfieCaptureScreen(
        //         jobId = jobId,
        //         allowAgentMode = allowAgentMode,
        //     )
        // }
    }
}

@Destination<SelfieGraph>()
@Composable
internal fun SmileSmartSelfieInstructionsScreen(
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    showAttribution: Boolean = true,
) {
    SmartSelfieInstructionsScreen(
        modifier = modifier,
        showAttribution = showAttribution,
    ) {
        navigator.navigate(direction = SmileSelfieCaptureScreenDestination)
    }
}

@Destination<SelfieGraph>()
@Composable
internal fun SmileSelfieCaptureScreen() {
    SelfieCaptureScreen()
}

@Destination<SelfieGraph>()
@Composable
internal fun SmileProcessingScreen() {
    // ProcessingScreen()
}
