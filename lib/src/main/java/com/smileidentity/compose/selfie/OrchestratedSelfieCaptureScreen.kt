package com.smileidentity.compose.selfie

import android.graphics.BitmapFactory
import android.os.OperationCanceledException
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smileidentity.R
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ImageCaptureConfirmationDialog
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.SelfieViewModel
import com.smileidentity.viewmodel.viewModelFactory
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * Orchestrates the selfie capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrchestratedSelfieCaptureScreen(
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = false,
    skipApiSubmission: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    viewModel: SelfieViewModel = viewModel(
        factory = viewModelFactory {
            SelfieViewModel(
                isEnroll = isEnroll,
                userId = userId,
                jobId = jobId,
                allowNewEnroll = allowNewEnroll,
                skipApiSubmission = skipApiSubmission,
                extraPartnerParams = extraPartnerParams,
            )
        },
    ),
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // TODO: Define sealed interfaces for each screen
    // todo: move this to uiState
    val startingDestination = if (showInstructions) "instructions" else "capture"
    // TODO: Allow disabling the top bar? e.g. for document verification
    LaunchedEffect(uiState.progress, uiState.selfieToConfirm) {
        if (uiState.progress == 1f && uiState.selfieToConfirm != null) {
            navController.navigate("confirmation")
        }
    }
    Box {
        Scaffold {
            NavHost(
                navController = navController,
                startDestination = startingDestination,
                modifier = Modifier
                    .padding(it)
                    .consumeWindowInsets(it),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
            ) {
                composable("instructions") {
                    SmartSelfieInstructionsScreen(
                        modifier = modifier,
                        showAttribution = showAttribution,
                    ) {
                        navController.navigate("capture")
                    }
                }
                composable("capture") {
                    SelfieCaptureScreen(
                        modifier = modifier,
                        imageAnalyzer = viewModel::analyzeImage,
                        captureProgress = uiState.progress,
                        directive = stringResource(uiState.directive.displayText),
                        allowAgentMode = allowAgentMode,
                    )
                }
                composable("confirmation") {
                    val selfieToConfirm = uiState.selfieToConfirm ?: return@composable
                    val painter = remember {
                        val path = selfieToConfirm.absolutePath
                        try {
                            BitmapPainter(BitmapFactory.decodeFile(path).asImageBitmap())
                        } catch (e: Exception) {
                            SmileIDCrashReporting.hub.addBreadcrumb(
                                "Error loading document image at $path",
                            )
                            SmileIDCrashReporting.hub.captureException(e)
                            ColorPainter(Color.Black)
                        }
                    }
                    ImageCaptureConfirmationDialog(
                        titleText = stringResource(
                            R.string.si_smart_selfie_confirmation_dialog_title,
                        ),
                        subtitleText = stringResource(
                            R.string.si_smart_selfie_confirmation_dialog_subtitle,
                        ),
                        painter = painter,
                        confirmButtonText = stringResource(
                            R.string.si_smart_selfie_confirmation_dialog_confirm_button,
                        ),
                        onConfirm = {
                            viewModel.submitJob()
                            navController.navigate("processing")
                        },
                        retakeButtonText = stringResource(
                            R.string.si_smart_selfie_confirmation_dialog_retake_button,
                        ),
                        onRetake = {
                            viewModel.clearPreviousCapture()
                            navController.popBackStack()
                        },
                        scaleFactor = 1.25f,
                    )
                }
                composable("processing") {
                    val processingState = uiState.processingState ?: return@composable
                    ProcessingScreen(
                        processingState = processingState,
                        inProgressTitle = stringResource(R.string.si_smart_selfie_processing_title),
                        inProgressSubtitle = stringResource(
                            R.string.si_smart_selfie_processing_subtitle,
                        ),
                        inProgressIcon = painterResource(
                            R.drawable.si_smart_selfie_processing_hero,
                        ),
                        successTitle = stringResource(
                            R.string.si_smart_selfie_processing_success_title,
                        ),
                        successSubtitle = stringResource(
                            uiState.errorMessage
                                ?: R.string.si_smart_selfie_processing_success_subtitle,
                        ),
                        successIcon = painterResource(R.drawable.si_processing_success),
                        errorTitle = stringResource(
                            R.string.si_smart_selfie_processing_error_title,
                        ),
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
        TopAppBar(
            colors = topAppBarColors(containerColor = Color.Transparent),
            title = {},
            navigationIcon = {
                // We don't show a back icon because it may be confusing to the user if the hosting
                // app also has its own top level back button
            },
            actions = {
                IconButton(
                    onClick = {
                        val exception = OperationCanceledException("User Cancelled")
                        onResult(SmileIDResult.Error(exception))
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.si_cd_close),
                    )
                }
            },
        )
    }
}
