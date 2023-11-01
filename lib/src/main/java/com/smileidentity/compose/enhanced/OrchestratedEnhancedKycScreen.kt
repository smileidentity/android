package com.smileidentity.compose.enhanced

import android.os.OperationCanceledException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.IdTypeSelectorAndFieldInputScreen
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.consent.OrchestratedConsentScreen
import com.smileidentity.models.JobType
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.EnhancedKycViewModel
import com.smileidentity.viewmodel.viewModelFactory
import java.net.URL

@Composable
internal fun OrchestratedEnhancedKycScreen(
    partnerIcon: Painter,
    partnerName: String,
    productName: String,
    partnerPrivacyPolicy: URL,
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    viewModel: EnhancedKycViewModel = viewModel(
        factory = viewModelFactory { EnhancedKycViewModel(
            userId = userId,
            jobId = jobId
        ) },
    ),
    onResult: SmileIDCallback<EnhancedKycResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        when {
            uiState.showLoading ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            uiState.showConsent -> OrchestratedConsentScreen(
                partnerIcon = partnerIcon,
                partnerName = partnerName,
                productName = productName,
                partnerPrivacyPolicy = partnerPrivacyPolicy,
                showAttribution = showAttribution,
                modifier = Modifier,
                onConsentGranted = viewModel::onConsentGranted,
                onConsentDenied = {
                    onResult(
                        SmileIDResult.Error(OperationCanceledException("User did not consent")),
                    )
                },
            )

            uiState.processingState != null -> ProcessingScreen(
                processingState = uiState.processingState,
                inProgressTitle = stringResource(R.string.si_enhanced_kyc_processing_title),
                inProgressSubtitle = stringResource(R.string.si_enhanced_kyc_processing_subtitle),
                inProgressIcon = rememberVectorPainter(Icons.Default.MailOutline),
                successTitle = stringResource(R.string.si_enhanced_kyc_processing_success_title),
                successSubtitle = stringResource(
                    R.string.si_enhanced_kyc_processing_success_subtitle,
                ),
                successIcon = rememberVectorPainter(Icons.Default.Done),
                errorTitle = stringResource(R.string.si_enhanced_kyc_processing_error_title),
                errorSubtitle = uiState.errorMessage
                    ?: stringResource(R.string.si_enhanced_kyc_processing_error_subtitle),
                errorIcon = rememberVectorPainter(Icons.Default.Warning),
                continueButtonText = stringResource(
                    R.string.si_enhanced_kyc_processing_continue_button,
                ),
                onContinue = { viewModel.onFinished(onResult) },
                retryButtonText = stringResource(R.string.si_enhanced_kyc_processing_retry_button),
                onRetry = { viewModel.onRetry() },
                closeButtonText = stringResource(R.string.si_enhanced_kyc_processing_close_button),
                onClose = { viewModel.onFinished(onResult) },
            )

            else -> IdTypeSelectorAndFieldInputScreen(
                jobType = JobType.EnhancedKyc,
                onResult = { viewModel.onIdInfoReceived(it) },
                modifier = Modifier,
            )
        }
    }
}
