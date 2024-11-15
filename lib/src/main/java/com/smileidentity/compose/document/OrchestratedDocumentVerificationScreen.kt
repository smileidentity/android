package com.smileidentity.compose.document

import android.os.OperationCanceledException
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.compose.nav.DocumentCaptureParams
import com.smileidentity.compose.nav.DocumentInstructionParams
import com.smileidentity.compose.nav.ImageConfirmParams
import com.smileidentity.compose.nav.NavigationBackHandler
import com.smileidentity.compose.nav.OrchestratedSelfieCaptureParams
import com.smileidentity.compose.nav.ProcessingScreenParams
import com.smileidentity.compose.nav.ResultCallbacks
import com.smileidentity.compose.nav.Routes
import com.smileidentity.compose.nav.SelfieCaptureParams
import com.smileidentity.compose.nav.encodeUrl
import com.smileidentity.compose.nav.getSelfieCaptureRoute
import com.smileidentity.compose.nav.localNavigationState
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.StringResource
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.document.OrchestratedDocumentUiState
import com.smileidentity.viewmodel.document.OrchestratedDocumentViewModel
import java.io.File

/**
 * Orchestrates the document capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun <T : Parcelable> OrchestratedDocumentVerificationScreen(
    resultCallbacks: ResultCallbacks,
    content: @Composable () -> Unit,
    viewModel: OrchestratedDocumentViewModel<T>,
    showSkipButton: Boolean,
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var acknowledgedBackInstructions by rememberSaveable { mutableStateOf(false) }

    resultCallbacks.apply {
        onDocumentFrontCaptureSuccess = viewModel::onFrontDocCaptured
        onDocumentBackCaptureSuccess = viewModel::onBackDocCaptured
        onDocumentCaptureError = viewModel::onError
        onDocumentBackSkip = viewModel::onDocumentBackSkip
        onProcessingContinue = { viewModel.onFinished(onResult) }
        onProcessingClose = { viewModel.onFinished(onResult) }
        onProcessingRetry = viewModel::onRetry
        onDocumentInstructionSkip = viewModel::onDocumentBackSkip
        onImageDialogRetake = {
            viewModel.onRestart()
            localNavigationState.screensNavigation.getNavController.popBackStack()
            if (uiState.currentStep is DocumentCaptureFlow.FrontDocumentCapture) {
                localNavigationState.screensNavigation.getNavController.popBackStack()
                navigateToDocumentCaptureScreen(
                    R.drawable.si_doc_v_front_hero,
                    R.string.si_doc_v_instruction_title,
                    R.string.si_verify_identity_instruction_subtitle,
                    R.string.si_doc_v_capture_instructions_front_title,
                    showSkipButton,
                    userId,
                    jobId,
                    showInstructions,
                    showAttribution,
                    allowGalleryUpload,
                    idAspectRatio,
                )
            } else if (uiState.currentStep is DocumentCaptureFlow.BackDocumentCapture) {
                navigateToDocumentCaptureScreen(
                    R.drawable.si_doc_v_back_hero,
                    R.string.si_doc_v_instruction_title,
                    R.string.si_verify_identity_instruction_subtitle,
                    R.string.si_doc_v_capture_instructions_back_title,
                    showSkipButton,
                    userId,
                    jobId,
                    showInstructions,
                    showAttribution,
                    allowGalleryUpload,
                    idAspectRatio,
                )
            }
        }
        onSmartSelfieResult = { result ->
            when (result) {
                is SmileIDResult.Error -> viewModel.onError(result.throwable)
                is SmileIDResult.Success -> viewModel.onSelfieCaptureSuccess(result)
            }
            localNavigationState.orchestratedNavigation.getNavController.popBackStack()
        }
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

    if (uiState.currentStep is DocumentCaptureFlow.FrontDocumentCapture) {
        resultCallbacks.onInstructionsAcknowledgedTakePhoto = {
            navigateToDocumentCaptureScreen(
                R.drawable.si_doc_v_front_hero,
                R.string.si_doc_v_instruction_title,
                R.string.si_verify_identity_instruction_subtitle,
                R.string.si_doc_v_capture_instructions_front_title,
                showSkipButton,
                userId,
                jobId,
                showInstructions,
                showAttribution,
                allowGalleryUpload,
                idAspectRatio,
                null,
            )
        }
        resultCallbacks.onConfirmCapturedImage = viewModel::onDocumentFrontCaptureSuccess
        resultCallbacks.onDocumentInstructionAcknowledgedSelectFromGallery = { uri ->
            navigateToDocumentCaptureScreen(
                R.drawable.si_doc_v_front_hero,
                R.string.si_doc_v_instruction_title,
                R.string.si_verify_identity_instruction_subtitle,
                R.string.si_doc_v_capture_instructions_front_title,
                showSkipButton,
                userId,
                jobId,
                showInstructions,
                showAttribution,
                allowGalleryUpload,
                idAspectRatio,
                uri,
            )
        }
    } else if (uiState.currentStep is DocumentCaptureFlow.BackDocumentCapture) {
        resultCallbacks.onInstructionsAcknowledgedTakePhoto = {
            acknowledgedBackInstructions = true
        }
        resultCallbacks.onConfirmCapturedImage = viewModel::onDocumentBackCaptureSuccess
    }

    HandleDocumentCaptureFlow(
        currentStep = uiState.currentStep,
        uiState = uiState,
        showInstructions = showInstructions,
        acknowledgedBackInstructions = acknowledgedBackInstructions,
        showAttribution = showAttribution,
        showSkipButton = showSkipButton,
        allowGalleryUpload = allowGalleryUpload,
        userId = userId,
        jobId = jobId,
        idAspectRatio = idAspectRatio,
        allowAgentMode = allowAgentMode,
    )

    NavigationBackHandler(
        navController = localNavigationState.screensNavigation.getNavController,
    ) { _, canGoBack ->
        localNavigationState.screensNavigation.getNavController.popBackStack()
        if (!canGoBack) {
            onResult(SmileIDResult.Error(OperationCanceledException("User cancelled")))
        }
    }
}

@Composable
private fun HandleDocumentCaptureFlow(
    currentStep: DocumentCaptureFlow,
    uiState: OrchestratedDocumentUiState,
    showInstructions: Boolean,
    acknowledgedBackInstructions: Boolean,
    showAttribution: Boolean,
    showSkipButton: Boolean,
    allowGalleryUpload: Boolean,
    userId: String,
    jobId: String,
    idAspectRatio: Float?,
    allowAgentMode: Boolean,
) {
    when (currentStep) {
        DocumentCaptureFlow.FrontDocumentCapture -> {
            HandleFrontDocumentCapture(
                uiState.documentFrontFile,
            )
        }

        DocumentCaptureFlow.BackDocumentCapture -> HandleBackDocumentCapture(
            showInstructions,
            acknowledgedBackInstructions,
            uiState.documentBackFile,
            showAttribution,
            showSkipButton,
            allowGalleryUpload,
            userId,
            jobId,
            idAspectRatio,
        )

        DocumentCaptureFlow.SelfieCapture -> HandleSelfieCapture(
            userId,
            jobId,
            showInstructions,
            showAttribution,
            allowAgentMode,
        )

        is DocumentCaptureFlow.ProcessingScreen -> HandleProcessingScreen(
            currentStep.processingState,
            uiState.errorMessage,
        )
    }
}

@Composable
private fun HandleFrontDocumentCapture(documentFrontFile: File?) {
    when {
        documentFrontFile != null -> NavigateToImageConfirmDialog(documentFrontFile)
    }
}

@Composable
private fun HandleBackDocumentCapture(
    showInstructions: Boolean,
    acknowledgedBackInstructions: Boolean,
    documentBackFile: File?,
    showAttribution: Boolean,
    showSkipButton: Boolean,
    allowGalleryUpload: Boolean,
    userId: String,
    jobId: String,
    idAspectRatio: Float?,
) {
    when {
        showInstructions && !acknowledgedBackInstructions -> NavigateToInstructionScreen(
            R.drawable.si_doc_v_back_hero,
            R.string.si_doc_v_instruction_back_title,
            R.string.si_doc_v_instruction_back_subtitle,
            showAttribution,
            allowGalleryUpload,
            true,
        )

        documentBackFile != null -> NavigateToImageConfirmDialog(documentBackFile)
        else -> navigateToDocumentCaptureScreen(
            R.drawable.si_doc_v_back_hero,
            R.string.si_doc_v_instruction_back_title,
            R.string.si_doc_v_instruction_back_subtitle,
            R.string.si_doc_v_capture_instructions_back_title,
            showSkipButton,
            userId,
            jobId,
            showInstructions,
            showAttribution,
            allowGalleryUpload,
            idAspectRatio,
            null,
            false,
        )
    }
}

@Composable
private fun HandleSelfieCapture(
    userId: String,
    jobId: String,
    showInstructions: Boolean,
    showAttribution: Boolean,
    allowAgentMode: Boolean,
) {
    val selfieCaptureParams = SelfieCaptureParams(
        userId = userId,
        jobId = jobId,
        showInstructions = showInstructions,
        showAttribution = showAttribution,
        allowAgentMode = allowAgentMode,
        skipApiSubmission = true,
    )
    val selfieStartRoute = getSelfieCaptureRoute(false, selfieCaptureParams)

    localNavigationState.orchestratedNavigation.navigateTo(
        Routes.Orchestrated.SelfieRoute(
            OrchestratedSelfieCaptureParams(
                selfieCaptureParams,
                startRoute = selfieStartRoute,
                showStartRoute = true,
            ),
        ),
    )
}

@Composable
private fun HandleProcessingScreen(
    processingState: ProcessingState,
    errorMessage: StringResource,
) {
    localNavigationState.screensNavigation.navigateTo(
        Routes.Shared.ProcessingScreen(
            ProcessingScreenParams(
                processingState = processingState,
                inProgressTitle = R.string.si_doc_v_processing_title,
                inProgressSubtitle = R.string.si_doc_v_processing_subtitle,
                inProgressIcon = R.drawable.si_doc_v_processing_hero,
                successTitle = R.string.si_doc_v_processing_success_title,
                successSubtitle = errorMessage.resolve().takeIf { it.isNotEmpty() }
                    ?: stringResource(R.string.si_doc_v_processing_success_subtitle),
                successIcon = R.drawable.si_processing_success,
                errorTitle = R.string.si_doc_v_processing_error_title,
                errorSubtitle = errorMessage.resolve().takeIf { it.isNotEmpty() }
                    ?: stringResource(id = R.string.si_processing_error_subtitle),
                errorIcon = R.drawable.si_processing_error,
                continueButtonText = R.string.si_continue,
                retryButtonText = R.string.si_smart_selfie_processing_retry_button,
                closeButtonText = R.string.si_smart_selfie_processing_close_button,
            ),
        ),
    )
}

@Composable
private fun NavigateToInstructionScreen(
    heroImage: Int,
    titleRes: Int,
    subtitleRes: Int,
    showAttribution: Boolean,
    allowGalleryUpload: Boolean,
    showSkipButton: Boolean,
) {
    localNavigationState.screensNavigation.navigateTo(
        Routes.Document.InstructionScreen(
            params = DocumentInstructionParams(
                heroImage = heroImage,
                title = stringResource(titleRes),
                subtitle = stringResource(subtitleRes),
                showAttribution = showAttribution,
                allowPhotoFromGallery = allowGalleryUpload,
                showSkipButton = showSkipButton,
            ),
        ),
    )
}

@Composable
private fun NavigateToImageConfirmDialog(documentFile: File) {
    localNavigationState.screensNavigation.navigateTo(
        Routes.Shared.ImageConfirmDialog(
            ImageConfirmParams(
                titleText = R.string.si_doc_v_confirmation_dialog_title,
                subtitleText = R.string.si_doc_v_confirmation_dialog_subtitle,
                imageFilePath = encodeUrl(documentFile.absolutePath),
                confirmButtonText = R.string.si_doc_v_confirmation_dialog_confirm_button,
                retakeButtonText = R.string.si_doc_v_confirmation_dialog_retake_button,
                scaleFactor = 1.0f,
            ),
        ),
    )
}

private fun navigateToDocumentCaptureScreen(
    heroImage: Int,
    titleRes: Int,
    subtitleRes: Int,
    captureTitleRes: Int,
    showSkipButton: Boolean,
    userId: String,
    jobId: String,
    showInstructions: Boolean,
    showAttribution: Boolean,
    allowGalleryUpload: Boolean,
    idAspectRatio: Float?,
    galleryDocumentUri: String? = null,
    front: Boolean = true,
) {
    val route = if (front) {
        Routes.Document.CaptureFrontScreen(
            DocumentCaptureParams(
                jobId = jobId,
                userId = userId,
                showInstructions = showInstructions,
                showAttribution = showAttribution,
                allowGallerySelection = allowGalleryUpload,
                showSkipButton = false,
                instructionsHeroImage = heroImage,
                instructionsTitleText = titleRes,
                instructionsSubtitleText = subtitleRes,
                captureTitleText = captureTitleRes,
                knownIdAspectRatio = idAspectRatio,
                galleryDocumentUri = galleryDocumentUri,
            ),
        )
    } else {
        Routes.Document.CaptureBackScreen(
            DocumentCaptureParams(
                jobId = jobId,
                userId = userId,
                showInstructions = showInstructions,
                showAttribution = showAttribution,
                allowGallerySelection = allowGalleryUpload,
                showSkipButton = showSkipButton,
                instructionsHeroImage = heroImage,
                instructionsTitleText = titleRes,
                instructionsSubtitleText = subtitleRes,
                captureTitleText = captureTitleRes,
                knownIdAspectRatio = idAspectRatio,
                galleryDocumentUri = galleryDocumentUri,
            ),
        )
    }
    localNavigationState.screensNavigation.navigateTo(
        route,
    )
}
