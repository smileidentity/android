package com.smileidentity.compose.document

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.OrchestratedEnhancedDocVViewModel
import com.smileidentity.viewmodel.viewModelFactory

@Composable
internal fun OrchestratedEnhancedDocVScreen(
    countryCode: String,
    documentType: String? = null,
    captureBothSides: Boolean = true,
    idAspectRatio: Float? = null,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowGalleryUpload: Boolean = true,
    showInstructions: Boolean = true,
    viewModel: OrchestratedEnhancedDocVViewModel = viewModel(
        factory = viewModelFactory {
            OrchestratedEnhancedDocVViewModel(
                userId = userId,
                jobId = jobId,
                countryCode = countryCode,
                documentType = documentType,
                captureBothSides = captureBothSides,
            )
        },
    ),
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val currentStep = uiState.currentStep) {
        DocumentCaptureFlow.FrontDocumentCapture -> DocumentCaptureScreen(
            side = DocumentCaptureSide.Front,
            showInstructions = showInstructions,
            showAttribution = showAttribution,
            allowGallerySelection = allowGalleryUpload,
            showSkipButton = false,
            instructionsTitleText = stringResource(R.string.si_doc_v_instruction_title),
            instructionsSubtitleText = stringResource(
                id = R.string.si_verify_identity_instruction_subtitle,
            ),
            captureTitleText = stringResource(
                id = R.string.si_doc_v_capture_instructions_front_title,
            ),
            knownIdAspectRatio = idAspectRatio,
            onConfirm = viewModel::onDocumentFrontCaptureSuccess,
            onError = viewModel::onError,
        )

        DocumentCaptureFlow.BackDocumentCapture -> DocumentCaptureScreen(
            side = DocumentCaptureSide.Back,
            showInstructions = showInstructions,
            showAttribution = showAttribution,
            allowGallerySelection = allowGalleryUpload,
            showSkipButton = captureBothSides,
            instructionsTitleText = stringResource(R.string.si_doc_v_instruction_back_title),
            instructionsSubtitleText = stringResource(
                id = R.string.si_doc_v_instruction_back_subtitle,
            ),
            captureTitleText = stringResource(
                id = R.string.si_doc_v_capture_instructions_back_title,
            ),
            knownIdAspectRatio = idAspectRatio,
            onConfirm = viewModel::onDocumentBackCaptureSuccess,
            onError = viewModel::onError,
            onSkip = viewModel::onDocumentBackSkip,
        )

        DocumentCaptureFlow.SelfieCapture -> OrchestratedSelfieCaptureScreen(
            userId = userId,
            jobId = jobId,
            isEnroll = false,
            allowAgentMode = false,
            showAttribution = showAttribution,
            showInstructions = showInstructions,
            skipApiSubmission = true,
        ) {
            when (it) {
                is SmileIDResult.Error -> viewModel.onError(it.throwable)
                is SmileIDResult.Success -> viewModel.onSelfieCaptureSuccess(it)
            }
        }

        is DocumentCaptureFlow.ProcessingScreen -> ProcessingScreen(
            processingState = currentStep.processingState,
            inProgressTitle = stringResource(R.string.si_doc_v_processing_title),
            inProgressSubtitle = stringResource(R.string.si_doc_v_processing_subtitle),
            inProgressIcon = painterResource(R.drawable.si_doc_v_processing_hero),
            successTitle = stringResource(R.string.si_doc_v_processing_success_title),
            successSubtitle = stringResource(R.string.si_doc_v_processing_success_subtitle),
            successIcon = painterResource(R.drawable.si_processing_success),
            errorTitle = stringResource(id = R.string.si_doc_v_processing_error_title),
            errorSubtitle = stringResource(
                uiState.errorMessage ?: R.string.si_processing_error_subtitle,
            ),
            errorIcon = painterResource(R.drawable.si_processing_error),
            continueButtonText = stringResource(R.string.si_continue),
            onContinue = { viewModel.onFinished(onResult) },
            retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
            onRetry = viewModel::onRetry,
            closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
            onClose = { viewModel.onFinished(onResult) },
        )
    }
}
