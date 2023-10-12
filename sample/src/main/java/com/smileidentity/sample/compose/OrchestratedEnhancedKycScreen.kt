package com.smileidentity.sample.compose

import android.os.OperationCanceledException
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.consent.OrchestratedConsentScreen
import com.smileidentity.models.JobType
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.sample.R
import com.smileidentity.sample.compose.components.IdTypeSelectorAndFieldInputScreen
import com.smileidentity.sample.viewmodel.EnhancedKycViewModel
import java.net.URL

@Composable
fun OrchestratedEnhancedKycScreen(
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    modifier: Modifier = Modifier,
    viewModel: EnhancedKycViewModel = viewModel(),
    showAttribution: Boolean = true,
    onResult: SmileIDCallback<EnhancedKycResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    when {
        uiState.showConsent -> OrchestratedConsentScreen(
            partnerIcon = partnerIcon,
            partnerName = partnerName,
            productName = productName,
            partnerPrivacyPolicy = partnerPrivacyPolicy,
            showAttribution = showAttribution,
            modifier = modifier,
            onConsentGranted = viewModel::onConsentGranted,
            onConsentDenied = {
                onResult(SmileIDResult.Error(OperationCanceledException("User did not consent")))
            },
        )
        uiState.processingState != null -> ProcessingScreen(
            processingState = uiState.processingState,
            inProgressTitle = stringResource(R.string.enhanced_kyc_processing_title),
            inProgressSubtitle = stringResource(R.string.enhanced_kyc_processing_subtitle),
            inProgressIcon = rememberVectorPainter(Icons.Default.MailOutline),
            successTitle = stringResource(R.string.enhanced_kyc_processing_success_title),
            successSubtitle = stringResource(R.string.enhanced_kyc_processing_success_subtitle),
            successIcon = rememberVectorPainter(Icons.Default.Done),
            errorTitle = stringResource(R.string.enhanced_kyc_processing_error_title),
            errorSubtitle = uiState.errorMessage
                ?: stringResource(R.string.enhanced_kyc_processing_error_subtitle),
            errorIcon = rememberVectorPainter(Icons.Default.Warning),
            continueButtonText = stringResource(R.string.enhanced_kyc_processing_continue_button),
            onContinue = { viewModel.onFinished(onResult) },
            retryButtonText = stringResource(R.string.enhanced_kyc_processing_retry_button),
            onRetry = { viewModel.onRetry() },
            closeButtonText = stringResource(R.string.enhanced_kyc_processing_close_button),
            onClose = { viewModel.onFinished(onResult) },
        )
        else -> IdTypeSelectorAndFieldInputScreen(
            jobType = JobType.EnhancedKyc,
            onResult = { viewModel.onIdInfoReceived(it) },
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun PreviewOrchestratedEnhancedKycScreen() {
    SmileIDTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            OrchestratedEnhancedKycScreen(
                partnerIcon = painterResource(
                    id = com.smileidentity.R.drawable.si_logo_with_text,
                ),
                partnerName = "Smile ID",
                productName = "BVN",
                partnerPrivacyPolicy = URL("https://usesmileid.com/privacy-policy")
            )
        }
    }
}
