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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.biometric.OrchestratedBiometricKYCScreen
import com.smileidentity.compose.components.ImageCaptureConfirmationDialog
import com.smileidentity.compose.components.LocalMetadata
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
import com.smileidentity.models.JobType
import com.smileidentity.viewmodel.document.DocumentVerificationViewModel
import com.smileidentity.viewmodel.viewModelFactory
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
    composable<Routes.Orchestrated.SelfieRoute>(
        typeMap = mapOf(
            typeOf<OrchestratedSelfieCaptureParams>() to CustomNavType(
                OrchestratedSelfieCaptureParams::class.java,
                OrchestratedSelfieCaptureParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.Orchestrated.SelfieRoute>()
        val params = route.params
        OrchestratedSelfieCaptureScreen(
            childNavController = childNavController,
            resultCallbacks = resultCallbacks,
            content = content,
            userId = params.captureParams.userId,
            jobId = params.captureParams.jobId,
            allowNewEnroll = params.captureParams.allowNewEnroll,
            isEnroll = params.captureParams.isEnroll,
            allowAgentMode = params.captureParams.allowAgentMode,
            skipApiSubmission = params.captureParams.skipApiSubmission,
            showAttribution = params.captureParams.showAttribution,
            showInstructions = params.captureParams.showInstructions,
            extraPartnerParams = params.captureParams.extraPartnerParams,
        )
    }
    composable<Routes.Orchestrated.BiometricKycRoute>(
        typeMap = mapOf(
            typeOf<OrchestratedBiometricCaptureParams>() to CustomNavType(
                OrchestratedBiometricCaptureParams::class.java,
                OrchestratedBiometricCaptureParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Orchestrated.BiometricKycRoute>()
        val params = route.params
        resultCallbacks.biometricKycViewModel?.let {
            OrchestratedBiometricKYCScreen(
                mainNavController = mainNavController,
                childNavController = childNavController,
                resultCallbacks = resultCallbacks,
                content = content,
                idInfo = params.captureParams.idInfo,
                userId = params.captureParams.userId,
                jobId = params.captureParams.jobId,
                allowNewEnroll = params.captureParams.allowNewEnroll,
                allowAgentMode = params.captureParams.allowAgentMode,
                showAttribution = params.captureParams.showAttribution,
                showInstructions = params.captureParams.showInstructions,
                extraPartnerParams = params.captureParams.extraPartnerParams,
                viewModel = it,
            )
        }
    }
    composable<Routes.Orchestrated.DocVRoute>(
        typeMap = mapOf(
            typeOf<OrchestratedDocumentParams>() to CustomNavType(
                OrchestratedDocumentParams::class.java,
                OrchestratedDocumentParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Orchestrated.DocVRoute>()
        val params = route.params
        val metadata = LocalMetadata.current
        OrchestratedDocumentVerificationScreen(
            mainNavController = mainNavController,
            childNavController = childNavController,
            resultCallbacks = resultCallbacks,
            content = content,
            userId = params.captureParams.userId,
            jobId = params.captureParams.jobId,
            showAttribution = params.captureParams.showAttribution,
            allowAgentMode = params.captureParams.allowAgentMode,
            allowGalleryUpload = params.captureParams.allowGallerySelection,
            showInstructions = params.captureParams.showInstructions,
            idAspectRatio = params.captureParams.knownIdAspectRatio,
            onResult = resultCallbacks.onDocVResult ?: {},
            viewModel = viewModel(
                factory = viewModelFactory {
                    DocumentVerificationViewModel(
                        jobType = JobType.DocumentVerification,
                        userId = params.captureParams.userId,
                        jobId = params.captureParams.jobId,
                        allowNewEnroll = params.captureParams.allowNewEnroll,
                        countryCode = params.captureParams.countryCode!!,
                        documentType = params.captureParams.documentType,
                        captureBothSides = params.captureParams.captureBothSides,
                        selfieFile = params.captureParams.selfieFile?.toFile(),
                        extraPartnerParams = params.captureParams.extraPartnerParams,
                        metadata = metadata,
                    )
                },
            ),
        )
    }
    composable<Routes.Orchestrated.EnhancedDocVRoute>(
        typeMap = mapOf(
            typeOf<OrchestratedDocumentParams>() to CustomNavType(
                OrchestratedDocumentParams::class.java,
                OrchestratedDocumentParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Orchestrated.EnhancedDocVRoute>()
        val params = route.params
        resultCallbacks.enhancedDocVViewModel?.let {
            OrchestratedDocumentVerificationScreen(
                mainNavController = mainNavController,
                childNavController = childNavController,
                resultCallbacks = resultCallbacks,
                content = content,
                userId = params.captureParams.userId,
                jobId = params.captureParams.jobId,
                showAttribution = params.captureParams.showAttribution,
                allowAgentMode = params.captureParams.allowAgentMode,
                allowGalleryUpload = params.captureParams.allowGallerySelection,
                showInstructions = params.captureParams.showInstructions,
                idAspectRatio = params.captureParams.knownIdAspectRatio,
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
    sharedDestinations(resultCallbacks)
    selfieDestinations(resultCallbacks)
    documentsDestinations(resultCallbacks, navController)
    nestedDocumentsDestinations(resultCallbacks)
}

internal fun NavGraphBuilder.documentsDestinations(
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
    navController: NavController,
) {
    composable<Routes.Document.CaptureFrontScreen>(
        typeMap = mapOf(
            typeOf<DocumentCaptureParams>() to CustomNavType(
                DocumentCaptureParams::class.java,
                DocumentCaptureParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Document.CaptureFrontScreen>()
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

    composable<Routes.Document.CaptureBackScreen>(
        typeMap = mapOf(
            typeOf<DocumentCaptureParams>() to CustomNavType(
                DocumentCaptureParams::class.java,
                DocumentCaptureParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Document.CaptureBackScreen>()
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

internal fun NavGraphBuilder.nestedDocumentsDestinations(
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
    navigation<DocScreens>(
        startDestination = Routes.Document.FrontInstructionScreen,
    ) {
        composable<Routes.Document.FrontInstructionScreen> { navBackStackEntry ->
            // val route = navBackStackEntry.toRoute<Routes.Document.InstructionScreen>()
            // val params = route.params
            DocumentCaptureInstructionsScreen(
                heroImage = R.drawable.si_doc_v_front_hero,
                title = stringResource(R.string.si_doc_v_instruction_title),
                subtitle = stringResource(
                    id = R.string.si_verify_identity_instruction_subtitle,
                ),
                showAttribution = true,
                allowPhotoFromGallery = false,
                showSkipButton = false,
                onSkip = resultCallbacks.onDocumentInstructionSkip ?: {},
                onInstructionsAcknowledgedSelectFromGallery =
                resultCallbacks.onDocumentInstructionAcknowledgedSelectFromGallery
                    ?: {},
                onInstructionsAcknowledgedTakePhoto =
                resultCallbacks.onInstructionsAcknowledgedTakePhoto
                    ?: {},
            )
        }

        composable<Routes.Document.InstructionScreen>(
            typeMap = mapOf(
                typeOf<DocumentInstructionParams>() to CustomNavType(
                    DocumentInstructionParams::class.java,
                    DocumentInstructionParams.serializer(),
                ),
            ),
        ) { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<Routes.Document.InstructionScreen>()
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

        composable<Routes.Document.CaptureScreenContent>(
            typeMap = mapOf(
                typeOf<DocumentCaptureContentParams>() to CustomNavType(
                    DocumentCaptureContentParams::class.java,
                    DocumentCaptureContentParams.serializer(),
                ),
            ),
        ) { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<Routes.Document.CaptureScreenContent>()
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
    }
}

internal fun NavGraphBuilder.selfieDestinations(
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
    composable<Routes.Selfie.CaptureScreen>(
        typeMap = mapOf(
            typeOf<SelfieCaptureParams>() to CustomNavType(
                SelfieCaptureParams::class.java,
                SelfieCaptureParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.Selfie.CaptureScreen>()
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

    composable<Routes.Selfie.InstructionsScreen>(
        typeMap = mapOf(
            typeOf<InstructionScreenParams>() to CustomNavType(
                InstructionScreenParams::class.java,
                InstructionScreenParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.Selfie.InstructionsScreen>()
        val params = route.params
        SmartSelfieInstructionsScreen(
            showAttribution = params.showAttribution,
            onInstructionsAcknowledged = resultCallbacks.onSelfieInstructionScreen ?: {},
        )
    }
}

internal fun NavGraphBuilder.sharedDestinations(
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
    dialog<Routes.Shared.ImageConfirmDialog>(
        typeMap = mapOf(
            typeOf<ImageConfirmParams>() to CustomNavType(
                ImageConfirmParams::class.java,
                ImageConfirmParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.Shared.ImageConfirmDialog>()
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
    composable<Routes.Shared.ProcessingScreen>(
        typeMap = mapOf(
            typeOf<ProcessingScreenParams>() to CustomNavType(
                ProcessingScreenParams::class.java,
                ProcessingScreenParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route: Routes.Shared.ProcessingScreen = backStackEntry.toRoute()
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
}
