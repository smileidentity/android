package com.smileidentity.compose.document

import android.os.Parcelable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
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
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box {
        Scaffold {
            NavHost(
                navController = navController,
                startDestination = "font_document_capture",
                modifier = Modifier
                    .padding(it)
                    .consumeWindowInsets(it),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
            ) {
                composable("front_document_capture") {
                    DocumentCaptureScreen(
                        jobId = jobId,
                        side = DocumentCaptureSide.Front,
                        showInstructions = showInstructions,
                        showAttribution = showAttribution,
                        allowGallerySelection = allowGalleryUpload,
                        showSkipButton = false,
                        instructionsHeroImage = R.drawable.si_doc_v_front_hero,
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
                }
                composable("back_document_capture") {
                    DocumentCaptureScreen(
                        jobId = jobId,
                        side = DocumentCaptureSide.Back,
                        showInstructions = showInstructions,
                        showAttribution = showAttribution,
                        allowGallerySelection = allowGalleryUpload,
                        showSkipButton = false,
                        instructionsHeroImage = R.drawable.si_doc_v_back_hero,
                        instructionsTitleText = stringResource(
                            R.string.si_doc_v_instruction_back_title,
                        ),
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
                }
                composable("selfie_capture") {
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
                composable("processing") {
                    val currentStep = uiState.currentStep as? DocumentCaptureFlow.ProcessingScreen
                        ?: return@composable
                    ProcessingScreen(
                        processingState = currentStep.processingState,
                        inProgressTitle = stringResource(R.string.si_doc_v_processing_title),
                        inProgressSubtitle = stringResource(R.string.si_doc_v_processing_subtitle),
                        inProgressIcon = painterResource(R.drawable.si_doc_v_processing_hero),
                        successTitle = stringResource(R.string.si_doc_v_processing_success_title),
                        successSubtitle = stringResource(
                            uiState.errorMessage ?: R.string.si_doc_v_processing_success_subtitle,
                        ),
                        successIcon = painterResource(R.drawable.si_processing_success),
                        errorTitle = stringResource(id = R.string.si_doc_v_processing_error_title),
                        errorSubtitle = stringResource(
                            uiState.errorMessage ?: R.string.si_processing_error_subtitle,
                        ),
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
    }
}
