package com.smileidentity.compose.biometric

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.compose.selfie.enhanced.OrchestratedSelfieCaptureScreenEnhanced
import com.smileidentity.metadata.LocalMetadataProvider
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.models.ConsentInformation
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
fun OrchestratedBiometricKYCScreen(
    idInfo: IdInfo,
    consentInformation: ConsentInformation?,
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    useStrictMode: Boolean = false,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    metadata: SnapshotStateList<Metadatum> = LocalMetadataProvider.current,
    viewModel: BiometricKycViewModel = viewModel(
        factory = viewModelFactory {
            BiometricKycViewModel(
                idInfo = idInfo,
                userId = userId,
                jobId = jobId,
                allowNewEnroll = allowNewEnroll,
                useStrictMode = useStrictMode,
                extraPartnerParams = extraPartnerParams,
                consentInformation = consentInformation,
                metadata = metadata,
            )
        },
    ),
    onResult: SmileIDCallback<BiometricKycResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val selfieCaptureScreen = @Composable {
        if (useStrictMode) {
            val context = LocalContext.current
            OrchestratedSelfieCaptureScreenEnhanced(
                userId = userId,
                allowNewEnroll = false,
                showInstructions = showInstructions,
                isEnroll = false,
                showAttribution = showAttribution,
                skipApiSubmission = true,
                onResult = { result ->
                    when (result) {
                        is SmileIDResult.Error -> {
                            onResult(result)
                        }
                        is SmileIDResult.Success -> {
                            viewModel.onSelfieCaptured(
                                selfieFile = result.data.selfieFile,
                                livenessFiles = result.data.livenessFiles,
                            )
                        }
                    }
                },
            )
        } else {
            OrchestratedSelfieCaptureScreen(
                userId = userId,
                jobId = jobId,
                isEnroll = false,
                allowAgentMode = allowAgentMode,
                showAttribution = showAttribution,
                showInstructions = showInstructions,
                skipApiSubmission = true,
                onResult = { result ->
                    when (result) {
                        is SmileIDResult.Error -> onResult(result)
                        is SmileIDResult.Success -> viewModel.onSelfieCaptured(
                            selfieFile = result.data.selfieFile,
                            livenessFiles = result.data.livenessFiles,
                        )
                    }
                },
            )
        }
    }
    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        when {
            uiState.processingState != null -> ProcessingScreen(
                processingState = uiState.processingState,
                inProgressTitle = stringResource(R.string.si_biometric_kyc_processing_title),
                inProgressSubtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
                inProgressIcon = painterResource(R.drawable.si_smart_selfie_processing_hero),
                successTitle = stringResource(R.string.si_biometric_kyc_processing_success_title),
                successSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                    ?: stringResource(R.string.si_biometric_kyc_processing_success_subtitle),
                successIcon = painterResource(R.drawable.si_processing_success),
                errorTitle = stringResource(R.string.si_biometric_kyc_processing_error_subtitle),
                errorSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                    ?: stringResource(id = R.string.si_processing_error_subtitle),
                errorIcon = painterResource(R.drawable.si_processing_error),
                continueButtonText = stringResource(R.string.si_continue),
                onContinue = { viewModel.onFinished(onResult) },
                retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
                onRetry = { viewModel.onRetry() },
                closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
                onClose = { viewModel.onFinished(onResult) },
            )

            else -> selfieCaptureScreen()
        }
    }
}
