package com.smileidentity.compose.nav

import android.graphics.BitmapFactory
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.biometric.OrchestratedBiometricKYCScreen
import com.smileidentity.compose.components.ImageCaptureConfirmationDialog
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.components.SmileThemeSurface
import com.smileidentity.compose.document.CaptureScreenContent
import com.smileidentity.compose.document.DocumentCaptureInstructionsScreen
import com.smileidentity.compose.document.DocumentCaptureScreen
import com.smileidentity.compose.document.DocumentCaptureSide
import com.smileidentity.compose.document.OrchestratedDocumentVerificationScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.compose.selfie.SelfieCaptureScreen
import com.smileidentity.compose.selfie.SmartSelfieInstructionsScreen
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import kotlin.reflect.typeOf

internal typealias MainGraphType = NavGraphBuilder.(
    NavHostController,
    NavHostController,
    ResultCallbacks,
    @Composable () -> Unit,
) -> Unit

@Composable
internal fun BaseSmileIDScreen(
    orchestratedDestination: Routes,
    screenDestination: Routes,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    content: MainGraphType,
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        val mainNavController = rememberNavController()
        val childNavController = rememberNavController()
        val resultCallbacks = remember { ResultCallbacks() }
        val childNavHost: @Composable () -> Unit = {
            NavHost(
                navController = childNavController,
                startDestination = screenDestination,
            ) {
                screensNavGraph(childNavController, resultCallbacks)
            }
        }
        NavHost(
            navController = mainNavController,
            startDestination = orchestratedDestination,
            modifier = modifier,
        ) {
            content(mainNavController, childNavController, resultCallbacks, childNavHost)
        }
    }
}

internal fun NavGraphBuilder.orchestratedNavGraph(
    mainNavController: NavController,
    childNavController: NavController,
    content: @Composable () -> Unit,
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
    composable<Routes.OrchestratedSelfieRoute>(
        typeMap = mapOf(
            typeOf<SelfieCaptureParams>() to CustomNavType(
                SelfieCaptureParams::class.java,
                SelfieCaptureParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.OrchestratedSelfieRoute>()
        val params = route.params
        OrchestratedSelfieCaptureScreen(
            childNavController = mainNavController,
            resultCallbacks = resultCallbacks,
            content = content,
            userId = params.userId,
            jobId = params.jobId,
            allowNewEnroll = params.allowNewEnroll,
            isEnroll = params.isEnroll,
            allowAgentMode = params.allowAgentMode,
            skipApiSubmission = params.skipApiSubmission,
            showAttribution = params.showAttribution,
            showInstructions = params.showInstructions,
            extraPartnerParams = params.extraPartnerParams,
        )
    }
    composable<Routes.OrchestratedBiometricKycRoute>(
        typeMap = mapOf(
            typeOf<OrchestratedBiometricKYCParams>() to CustomNavType(
                OrchestratedBiometricKYCParams::class.java,
                OrchestratedBiometricKYCParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.OrchestratedBiometricKycRoute>()
        val params = route.params
        resultCallbacks.biometricKycViewModel?.let {
            OrchestratedBiometricKYCScreen(
                mainNavController = mainNavController,
                childNavController = childNavController,
                resultCallbacks = resultCallbacks,
                content = content,
                idInfo = params.idInfo,
                userId = params.userId,
                jobId = params.jobId,
                allowNewEnroll = params.allowNewEnroll,
                allowAgentMode = params.allowAgentMode,
                showAttribution = params.showAttribution,
                showInstructions = params.showInstructions,
                extraPartnerParams = params.extraPartnerParams,
                viewModel = it,
            )
        }
    }
    composable<Routes.OrchestratedDocVRoute>(
        typeMap = mapOf(
            typeOf<DocumentCaptureParams>() to CustomNavType(
                DocumentCaptureParams::class.java,
                DocumentCaptureParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.OrchestratedDocVRoute>()
        val params = route.params
        resultCallbacks.documentViewModel?.let {
            OrchestratedDocumentVerificationScreen(
                mainNavController = mainNavController,
                childNavController = childNavController,
                resultCallbacks = resultCallbacks,
                content = content,
                userId = params.userId,
                jobId = params.jobId,
                showAttribution = params.showAttribution,
                allowAgentMode = params.allowAgentMode,
                allowGalleryUpload = params.allowGallerySelection,
                showInstructions = params.showInstructions,
                idAspectRatio = params.knownIdAspectRatio,
                onResult = resultCallbacks.onDocVResult ?: {},
                viewModel = it,
            )
        }
    }
    composable<Routes.OrchestratedEnhancedDocVRoute>(
        typeMap = mapOf(
            typeOf<DocumentCaptureParams>() to CustomNavType(
                DocumentCaptureParams::class.java,
                DocumentCaptureParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.OrchestratedEnhancedDocVRoute>()
        val params = route.params
        resultCallbacks.enhancedDocVViewModel?.let {
            OrchestratedDocumentVerificationScreen(
                mainNavController = mainNavController,
                childNavController = childNavController,
                resultCallbacks = resultCallbacks,
                content = content,
                userId = params.userId,
                jobId = params.jobId,
                showAttribution = params.showAttribution,
                allowAgentMode = params.allowAgentMode,
                allowGalleryUpload = params.allowGallerySelection,
                showInstructions = params.showInstructions,
                idAspectRatio = params.knownIdAspectRatio,
                onResult = resultCallbacks.onEnhancedDocVResult ?: {},
                viewModel = it,
            )
        }
    }
}

internal fun NavGraphBuilder.screensNavGraph(
    navController: NavController,
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
    composable<Routes.SelfieCaptureScreenRoute>(
        typeMap = mapOf(
            typeOf<SelfieCaptureParams>() to CustomNavType(
                SelfieCaptureParams::class.java,
                SelfieCaptureParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.SelfieCaptureScreenRoute>()
        val params = route.params
        resultCallbacks.selfieViewModel?.let {
            SelfieCaptureScreen(
                userId = params.userId,
                jobId = params.jobId,
                isEnroll = params.isEnroll,
                allowAgentMode = params.allowAgentMode,
                skipApiSubmission = params.skipApiSubmission,
                viewModel = it,
            )
        }
    }

    composable<Routes.SelfieInstructionsScreenRoute>(
        typeMap = mapOf(
            typeOf<InstructionScreenParams>() to CustomNavType(
                InstructionScreenParams::class.java,
                InstructionScreenParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.SelfieInstructionsScreenRoute>()
        val params = route.params
        SmartSelfieInstructionsScreen(
            showAttribution = params.showAttribution,
            onInstructionsAcknowledged = resultCallbacks.onSelfieInstructionScreen ?: {},
        )
    }

    composable<Routes.DocumentCaptureScreenContent>(
        typeMap = mapOf(
            typeOf<DocumentCaptureContentParams>() to CustomNavType(
                DocumentCaptureContentParams::class.java,
                DocumentCaptureContentParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.DocumentCaptureScreenContent>()
        val params = route.params
        CaptureScreenContent(
            titleText = stringResource(params.titleText),
            subtitleText = stringResource(params.subtitleText),
            idAspectRatio = params.idAspectRatio,
            areEdgesDetected = params.areEdgesDetected,
            showCaptureInProgress = params.showCaptureInProgress,
            showManualCaptureButton = params.showManualCaptureButton,
            onCaptureClicked = resultCallbacks.onCaptureClicked ?: {},
            imageAnalyzer = resultCallbacks.imageAnalyzer ?: { _, _ -> },
            onFocusEvent = resultCallbacks.onFocusEvent ?: {},
        )
    }

    dialog<Routes.ImageCaptureConfirmDialog>(
        typeMap = mapOf(
            typeOf<ImageConfirmParams>() to CustomNavType(
                ImageConfirmParams::class.java,
                ImageConfirmParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.ImageCaptureConfirmDialog>()
        val params = route.params
        val selfieUrl = decodeUrl(params.imageFilePath)
        ImageCaptureConfirmationDialog(
            titleText = stringResource(params.titleText),
            subtitleText = stringResource(params.subtitleText),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(selfieUrl).asImageBitmap(),
            ),
            confirmButtonText = stringResource(
                params.confirmButtonText,
            ),
            onConfirm = resultCallbacks.onConfirmCapturedImage ?: {},
            retakeButtonText = stringResource(
                params.retakeButtonText,
            ),
            onRetake = resultCallbacks.onImageDialogRetake ?: {},
            scaleFactor = 1.25f,
        )
    }
    composable<Routes.ProcessingScreenRoute>(
        typeMap = mapOf(
            typeOf<ProcessingScreenParams>() to CustomNavType(
                ProcessingScreenParams::class.java,
                ProcessingScreenParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route: Routes.ProcessingScreenRoute = backStackEntry.toRoute()
        val params = route.params
        ProcessingScreen(
            processingState = params.processingState,
            inProgressTitle = stringResource(params.inProgressTitle),
            inProgressSubtitle = stringResource(params.inProgressSubtitle),
            inProgressIcon = painterResource(R.drawable.si_doc_v_processing_hero),
            successTitle = stringResource(params.successTitle),
            successSubtitle = params.successSubtitle,
            successIcon = painterResource(R.drawable.si_processing_success),
            errorTitle = stringResource(params.errorTitle),
            errorSubtitle = params.errorSubtitle,
            errorIcon = painterResource(R.drawable.si_processing_error),
            continueButtonText = stringResource(params.continueButtonText),
            onContinue = resultCallbacks.onProcessingContinue ?: {},
            retryButtonText = stringResource(params.retryButtonText),
            onRetry = resultCallbacks.onProcessingRetry ?: {},
            closeButtonText = stringResource(params.closeButtonText),
            onClose = resultCallbacks.onProcessingClose ?: {},
        )
    }
    composable<Routes.DocumentInstructionRoute>(
        typeMap = mapOf(
            typeOf<DocumentInstructionParams>() to CustomNavType(
                DocumentInstructionParams::class.java,
                DocumentInstructionParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.DocumentInstructionRoute>()
        val params = route.params
        DocumentCaptureInstructionsScreen(
            heroImage = params.heroImage,
            title = params.title,
            subtitle = params.subtitle,
            showAttribution = params.showAttribution,
            allowPhotoFromGallery = params.allowPhotoFromGallery,
            showSkipButton = params.showSkipButton,
            onSkip = resultCallbacks.onDocumentInstructionSkip ?: {},
            onInstructionsAcknowledgedSelectFromGallery =
            resultCallbacks.onDocumentInstructionAcknowledgedSelectFromGallery
                ?: {},
            onInstructionsAcknowledgedTakePhoto =
            resultCallbacks.onInstructionsAcknowledgedTakePhoto
                ?: {},
        )
    }
    composable<Routes.DocumentCaptureFrontRoute>(
        typeMap = mapOf(
            typeOf<DocumentCaptureParams>() to CustomNavType(
                DocumentCaptureParams::class.java,
                DocumentCaptureParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.DocumentCaptureFrontRoute>()
        val params = route.params
        DocumentCaptureScreen(
            navController = navController,
            resultCallbacks = resultCallbacks,
            jobId = params.jobId,
            showInstructions = params.showInstructions,
            showAttribution = params.showAttribution,
            allowGallerySelection = params.allowGallerySelection,
            showSkipButton = params.showSkipButton,
            side = DocumentCaptureSide.Front,
            instructionsHeroImage = params.instructionsHeroImage,
            instructionsTitleText = stringResource(params.instructionsTitleText),
            knownIdAspectRatio = params.knownIdAspectRatio,
            instructionsSubtitleText = stringResource(params.instructionsSubtitleText),
            captureTitleText = stringResource(params.captureTitleText),
            onConfirm = resultCallbacks.onDocumentFrontCaptureSuccess ?: {},
            onError = resultCallbacks.onDocumentCaptureError ?: {},
        )
    }
    composable<Routes.DocumentCaptureBackRoute>(
        typeMap = mapOf(
            typeOf<DocumentCaptureParams>() to CustomNavType(
                DocumentCaptureParams::class.java,
                DocumentCaptureParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.DocumentCaptureFrontRoute>()
        val params = route.params
        DocumentCaptureScreen(
            navController = navController,
            resultCallbacks = resultCallbacks,
            jobId = params.jobId,
            showInstructions = params.showInstructions,
            showAttribution = params.showAttribution,
            allowGallerySelection = params.allowGallerySelection,
            showSkipButton = params.showSkipButton,
            side = DocumentCaptureSide.Back,
            instructionsHeroImage = params.instructionsHeroImage,
            instructionsTitleText = stringResource(params.instructionsTitleText),
            knownIdAspectRatio = params.knownIdAspectRatio,
            instructionsSubtitleText = stringResource(params.instructionsSubtitleText),
            captureTitleText = stringResource(params.captureTitleText),
            onConfirm = resultCallbacks.onDocumentBackCaptureSuccess ?: {},
            onError = resultCallbacks.onDocumentCaptureError ?: {},
        )
    }
}
