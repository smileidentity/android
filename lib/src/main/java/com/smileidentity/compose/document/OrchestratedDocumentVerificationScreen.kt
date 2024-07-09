package com.smileidentity.compose.document

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.compose.nav.DocumentCaptureScreenCaptureRoute
import com.smileidentity.compose.nav.DocumentCaptureSideNavType
import com.smileidentity.compose.nav.OrchestratedProcessingRoute
import com.smileidentity.compose.nav.OrchestratedSelfieCaptureScreenRoute
import com.smileidentity.compose.nav.ProcessingStateNavType
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.document.OrchestratedDocumentViewModel
import kotlin.reflect.typeOf

/**
 * Orchestrates the document capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun <T : Parcelable> OrchestratedDocumentVerificationScreen(
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val frontCapture = DocumentCaptureScreenCaptureRoute(
        aspectRatio = idAspectRatio ?: 1f,
        side = DocumentCaptureSide.Front,
        instructionsHeroImage = R.drawable.si_doc_v_front_hero,
        instructionsTitleText = stringResource(R.string.si_doc_v_instruction_title),
        instructionsSubtitleText = stringResource(R.string.si_verify_identity_instruction_subtitle),
        captureTitleText = stringResource(R.string.si_doc_v_capture_instructions_front_title),
    )
    val backCapture = DocumentCaptureScreenCaptureRoute(
        aspectRatio = idAspectRatio ?: 1f,
        side = DocumentCaptureSide.Back,
        instructionsHeroImage = R.drawable.si_doc_v_back_hero,
        instructionsTitleText = stringResource(R.string.si_doc_v_instruction_back_title),
        instructionsSubtitleText = stringResource(R.string.si_doc_v_instruction_back_subtitle),
        captureTitleText = stringResource(R.string.si_doc_v_capture_instructions_back_title),
    )

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        NavHost(navController, startDestination = frontCapture) {
            composable<DocumentCaptureScreenCaptureRoute>(
                typeMap = mapOf(
                    typeOf<DocumentCaptureSide>() to DocumentCaptureSideNavType,
                ),
            ) { backStackEntry ->
                val documentImageRoute = backStackEntry.toRoute<DocumentCaptureScreenCaptureRoute>()
                DocumentCaptureScreen(
                    jobId = jobId,
                    side = documentImageRoute.side,
                    showInstructions = showInstructions,
                    showAttribution = showAttribution,
                    allowGallerySelection = allowGalleryUpload,
                    showSkipButton = false,
                    instructionsHeroImage = documentImageRoute.instructionsHeroImage,
                    instructionsTitleText = documentImageRoute.instructionsTitleText,
                    instructionsSubtitleText = documentImageRoute.instructionsSubtitleText,
                    captureTitleText = documentImageRoute.captureTitleText,
                    knownIdAspectRatio = documentImageRoute.aspectRatio,
                    onConfirm = viewModel::onDocumentFrontCaptureSuccess,
                    onError = viewModel::onError,
                )
            }
            composable<OrchestratedSelfieCaptureScreenRoute>(
                typeMap = mapOf(
                    typeOf<ProcessingState>() to ProcessingStateNavType,
                ),
            ) {
                OrchestratedSelfieCaptureScreen(
                    userId = userId,
                    jobId = jobId,
                    isEnroll = false,
                    allowAgentMode = allowAgentMode,
                    showAttribution = showAttribution,
                    showInstructions = showInstructions,
                    skipApiSubmission = true,
                ) {
                    when (it) {
                        is SmileIDResult.Error -> viewModel.onError(it.throwable)
                        is SmileIDResult.Success -> viewModel.onSelfieCaptureSuccess(it)
                    }
                }
            }
            composable<OrchestratedProcessingRoute>(
                typeMap = mapOf(
                    typeOf<ProcessingState>() to ProcessingStateNavType,
                ),
            ) { backStackEntry ->
                val processingRoute: OrchestratedProcessingRoute = backStackEntry.toRoute()
                ProcessingScreen(
                    processingState = processingRoute.processingState,
                    inProgressTitle = stringResource(R.string.si_doc_v_processing_title),
                    inProgressSubtitle = stringResource(R.string.si_doc_v_processing_subtitle),
                    inProgressIcon = painterResource(R.drawable.si_doc_v_processing_hero),
                    successTitle = stringResource(R.string.si_doc_v_processing_success_title),
                    successSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                        ?: stringResource(R.string.si_doc_v_processing_success_subtitle),
                    successIcon = painterResource(R.drawable.si_processing_success),
                    errorTitle = stringResource(id = R.string.si_doc_v_processing_error_title),
                    errorSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
                        ?: stringResource(id = R.string.si_processing_error_subtitle),
                    errorIcon = painterResource(R.drawable.si_processing_error),
                    continueButtonText = stringResource(R.string.si_continue),
                    onContinue = { viewModel.onFinished(onResult) },
                    retryButtonText = stringResource(
                        R.string.si_smart_selfie_processing_retry_button,
                    ),
                    onRetry = viewModel::onRetry,
                    closeButtonText = stringResource(
                        R.string.si_smart_selfie_processing_close_button,
                    ),
                    onClose = { viewModel.onFinished(onResult) },
                )
            }
        }
    }
    when (val currentStep = uiState.currentStep) {
        is DocumentCaptureFlow.FrontDocumentCapture -> {
            navController.navigate(frontCapture)
        }

        is DocumentCaptureFlow.BackDocumentCapture -> {
            navController.navigate(backCapture)
        }

        is DocumentCaptureFlow.SelfieCapture -> {
            navController.navigate(OrchestratedSelfieCaptureScreenRoute)
        }

        is DocumentCaptureFlow.ProcessingScreen -> {
            navController.navigate(OrchestratedProcessingRoute(currentStep.processingState))
        }
    }
}
