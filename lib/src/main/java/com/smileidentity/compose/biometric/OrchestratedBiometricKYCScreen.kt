package com.smileidentity.compose.biometric

import android.os.OperationCanceledException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.consent.ConsentDeniedScreen
import com.smileidentity.compose.consent.ConsentScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.models.IdInfo
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.BiometricKycViewModel
import com.smileidentity.viewmodel.viewModelFactory
import java.net.URL

@Composable
fun OrchestratedBiometricKYCScreen(
    idInfo: IdInfo,
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowAgentMode: Boolean = false,
    showAttribution: Boolean = true,
    viewModel: BiometricKycViewModel = viewModel(
        factory = viewModelFactory { BiometricKycViewModel(idInfo, userId, jobId) },
    ),
    onResult: SmileIDCallback<BiometricKycResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    when {
        uiState.showLoading -> Box(modifier = modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        uiState.showConsent -> ConsentScreen(
            partnerIcon = partnerIcon,
            partnerName = partnerName,
            productName = productName,
            partnerPrivacyPolicy = partnerPrivacyPolicy,
            showAttribution = showAttribution,
            onContinue = { viewModel.onConsentGranted() },
            onCancel = { viewModel.onConsentDenied() },
        )

        uiState.consentDenied -> ConsentDeniedScreen(
            onGoBack = { viewModel.onConsentDeniedTryAgain() },
            onCancel = {
                onResult(SmileIDResult.Error(OperationCanceledException("User did not consent")))
            },
        )

        uiState.processingState != null -> ProcessingScreen(
            processingState = uiState.processingState,
            inProgressTitle = stringResource(R.string.si_biometric_kyc_processing_title),
            inProgressSubtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
            inProgressIcon = painterResource(R.drawable.si_smart_selfie_processing_hero),
            successTitle = stringResource(R.string.si_biometric_kyc_processing_success_title),
            successSubtitle = stringResource(R.string.si_biometric_kyc_processing_success_subtitle),
            successIcon = painterResource(R.drawable.si_processing_success),
            errorTitle = stringResource(R.string.si_biometric_kyc_processing_error_subtitle),
            errorSubtitle = stringResource(R.string.si_processing_error_subtitle),
            errorIcon = painterResource(R.drawable.si_processing_error),
            continueButtonText = stringResource(
                R.string.si_smart_selfie_processing_continue_button,
            ),
            onContinue = { viewModel.onFinished(onResult) },
            retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
            onRetry = { viewModel.onRetry() },
            closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
            onClose = { viewModel.onFinished(onResult) },
        )

        else -> OrchestratedSelfieCaptureScreen(
            userId = userId,
            jobId = jobId,
            allowAgentMode = allowAgentMode,
            skipApiSubmission = true,
        ) {
            when (it) {
                is SmileIDResult.Error -> onResult(it)
                is SmileIDResult.Success -> viewModel.onSelfieCaptured(
                    selfieFile = it.data.selfieFile,
                    livenessFiles = it.data.livenessFiles,
                )
            }
        }
    }
}
