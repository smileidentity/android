package com.smileidentity.compose.biometric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.nav.OrchestratedSelfieCaptureParams
import com.smileidentity.compose.nav.ProcessingScreenParams
import com.smileidentity.compose.nav.ResultCallbacks
import com.smileidentity.compose.nav.Routes
import com.smileidentity.compose.nav.SelfieCaptureParams
import com.smileidentity.compose.nav.localNavigationState
import com.smileidentity.models.IdInfo
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.BiometricKycViewModel
import com.smileidentity.viewmodel.viewModelFactory
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Composable
internal fun OrchestratedBiometricKYCScreen(
    resultCallbacks: ResultCallbacks,
    content: @Composable () -> Unit,
    idInfo: IdInfo,
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    viewModel: BiometricKycViewModel = viewModel(
        factory = viewModelFactory {
            BiometricKycViewModel(
                idInfo = idInfo,
                userId = userId,
                jobId = jobId,
                allowNewEnroll = allowNewEnroll,
                extraPartnerParams = extraPartnerParams,
            )
        },
    ),
    onResult: SmileIDCallback<BiometricKycResult> = {},
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
    resultCallbacks.onProcessingContinue = { viewModel.onFinished(onResult) }
    resultCallbacks.onProcessingClose = { viewModel.onFinished(onResult) }
    resultCallbacks.onSmartSelfieResult = {
        when (it) {
            is SmileIDResult.Error -> onResult(it)
            is SmileIDResult.Success -> viewModel.onSelfieCaptured(
                selfieFile = it.data.selfieFile,
                livenessFiles = it.data.livenessFiles,
            )
        }
        localNavigationState.orchestratedNavigation.getNavController.popBackStack()
    }
    when {
        uiState.processingState != null -> {
            localNavigationState.screensNavigation.navigateTo(
                Routes.Shared.ProcessingScreen(
                    ProcessingScreenParams(
                        processingState = uiState.processingState,
                        inProgressTitle = R.string.si_biometric_kyc_processing_title,
                        inProgressSubtitle = R.string.si_smart_selfie_processing_subtitle,
                        inProgressIcon = R.drawable.si_smart_selfie_processing_hero,
                        successTitle = R.string.si_biometric_kyc_processing_success_title,
                        successSubtitle = uiState.errorMessage.resolve().takeIf {
                            it.isNotEmpty()
                        } ?: stringResource(R.string.si_biometric_kyc_processing_success_subtitle),
                        successIcon = R.drawable.si_processing_success,
                        errorTitle = R.string.si_biometric_kyc_processing_error_subtitle,
                        errorSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                            ?: stringResource(id = R.string.si_processing_error_subtitle),
                        errorIcon = R.drawable.si_processing_error,
                        continueButtonText = R.string.si_continue,
                        retryButtonText = R.string.si_smart_selfie_processing_retry_button,
                        closeButtonText = R.string.si_smart_selfie_processing_close_button,
                    ),
                ),
                popUpTo = true,
                popUpToInclusive = true,
            )
        } else -> {
            localNavigationState.orchestratedNavigation.navigateTo(
                Routes.Orchestrated.SelfieRoute(
                    OrchestratedSelfieCaptureParams(
                        SelfieCaptureParams(
                            userId = userId,
                            jobId = jobId,
                            showInstructions = showInstructions,
                            showAttribution = showAttribution,
                            allowAgentMode = allowAgentMode,
                            skipApiSubmission = true,
                        ),
                    ),
                ),
                popUpTo = true,
                popUpToInclusive = true,
            )
        }
    }
}
