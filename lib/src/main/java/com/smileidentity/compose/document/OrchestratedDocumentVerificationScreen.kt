package com.smileidentity.compose.document

import android.os.Parcelable
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
import com.smileidentity.R
import com.smileidentity.compose.nav.DocumentCaptureParams
import com.smileidentity.compose.nav.OrchestratedSelfieCaptureParams
import com.smileidentity.compose.nav.ProcessingScreenParams
import com.smileidentity.compose.nav.ResultCallbacks
import com.smileidentity.compose.nav.Routes
import com.smileidentity.compose.nav.SelfieCaptureParams
import com.smileidentity.compose.nav.localNavigationState
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.document.OrchestratedDocumentViewModel

/**
 * Orchestrates the document capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun <T : Parcelable> OrchestratedDocumentVerificationScreen(
    resultCallbacks: ResultCallbacks,
    content: @Composable () -> Unit,
    viewModel: OrchestratedDocumentViewModel<T>,
    modifier: Modifier = Modifier,
    idAspectRatio: Float? = null,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowAgentMode: Boolean = false,
    allowGalleryUpload: Boolean = false,
    showInstructions: Boolean = true,
    onResult: SmileIDCallback<T> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    resultCallbacks.onDocumentFrontCaptureSuccess = viewModel::onDocumentFrontCaptureSuccess
    resultCallbacks.onDocumentBackCaptureSuccess = viewModel::onDocumentBackCaptureSuccess
    resultCallbacks.onDocumentCaptureError = viewModel::onError
    resultCallbacks.onDocumentBackSkip = viewModel::onDocumentBackSkip
    resultCallbacks.onInstructionsAcknowledgedTakePhoto = {
        Routes.Document.CaptureFrontScreen(
            DocumentCaptureParams(
                jobId = jobId,
                userId = userId,
                showInstructions = showInstructions,
                showAttribution = showAttribution,
                allowGallerySelection = allowGalleryUpload,
                showSkipButton = false,
                instructionsHeroImage = R.drawable.si_doc_v_front_hero,
                instructionsTitleText = R.string.si_doc_v_instruction_title,
                instructionsSubtitleText = R.string.si_verify_identity_instruction_subtitle,
                captureTitleText = R.string.si_doc_v_capture_instructions_front_title,
                knownIdAspectRatio = idAspectRatio,
            ),
        )
    }
    resultCallbacks.onProcessingContinue = { viewModel.onFinished(onResult) }
    resultCallbacks.onProcessingClose = { viewModel.onFinished(onResult) }
    resultCallbacks.onSmartSelfieResult = {
        when (it) {
            is SmileIDResult.Error -> viewModel.onError(it.throwable)
            is SmileIDResult.Success -> viewModel.onSelfieCaptureSuccess(it)
        }
        localNavigationState.orchestratedNavigation.getNavController.popBackStack()
    }
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        content()
    }
    when (val currentStep = uiState.currentStep) {
        DocumentCaptureFlow.FrontDocumentCapture ->
            localNavigationState.screensNavigation.navigateTo(
                Routes.Document.CaptureFrontScreen(
                    DocumentCaptureParams(
                        jobId = jobId,
                        userId = userId,
                        showInstructions = showInstructions,
                        showAttribution = showAttribution,
                        allowGallerySelection = allowGalleryUpload,
                        showSkipButton = false,
                        instructionsHeroImage = R.drawable.si_doc_v_front_hero,
                        instructionsTitleText = R.string.si_doc_v_instruction_title,
                        instructionsSubtitleText = R.string.si_verify_identity_instruction_subtitle,
                        captureTitleText = R.string.si_doc_v_capture_instructions_front_title,
                        knownIdAspectRatio = idAspectRatio,
                    ),
                ),
                popUpTo = true,
                popUpToInclusive = true,
            )

        DocumentCaptureFlow.BackDocumentCapture ->
            localNavigationState.screensNavigation.navigateTo(
                Routes.Document.CaptureBackScreen(
                    DocumentCaptureParams(
                        jobId = jobId,
                        userId = userId,
                        showInstructions = showInstructions,
                        showAttribution = showAttribution,
                        allowGallerySelection = allowGalleryUpload,
                        showSkipButton = false,
                        instructionsHeroImage = R.drawable.si_doc_v_back_hero,
                        instructionsTitleText = R.string.si_doc_v_instruction_back_title,
                        instructionsSubtitleText = R.string.si_doc_v_instruction_back_subtitle,
                        captureTitleText = R.string.si_doc_v_capture_instructions_back_title,
                        knownIdAspectRatio = idAspectRatio,
                    ),
                ),
                popUpTo = true,
                popUpToInclusive = true,
            )

        DocumentCaptureFlow.SelfieCapture ->
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
                popUpTo = false,
                popUpToInclusive = false,
            )

        is DocumentCaptureFlow.ProcessingScreen ->
            localNavigationState.screensNavigation.navigateTo(
                Routes.Shared.ProcessingScreen(
                    ProcessingScreenParams(
                        processingState = currentStep.processingState,
                        inProgressTitle = R.string.si_doc_v_processing_title,
                        inProgressSubtitle = R.string.si_doc_v_processing_subtitle,
                        inProgressIcon = R.drawable.si_doc_v_processing_hero,
                        successTitle = R.string.si_doc_v_processing_success_title,
                        successSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                            ?: stringResource(R.string.si_doc_v_processing_success_subtitle),
                        successIcon = R.drawable.si_processing_success,
                        errorTitle = R.string.si_doc_v_processing_error_title,
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
    }
}
