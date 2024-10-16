package com.smileidentity.compose.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.biometric.OrchestratedBiometricKYCScreen
import com.smileidentity.compose.components.ImageCaptureConfirmationDialog
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.components.SmileThemeSurface
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
import com.smileidentity.viewmodel.document.EnhancedDocumentVerificationViewModel
import com.smileidentity.viewmodel.viewModelFactory
import kotlin.reflect.typeOf

@Composable
internal fun BaseSmileIDScreen(
    orchestratedDestination: Routes,
    screenDestination: Routes,
    resultCallbacks: ResultCallbacks,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        localNavigationState = MultiNavigationStates(
            rootNavigation = rememberMultiNavigationAppState(
                startDestination = Routes.Root,
            ),
            orchestratedNavigation = rememberMultiNavigationAppState(
                startDestination = Routes.BaseOrchestrated,
            ),
            screensNavigation = rememberMultiNavigationAppState(
                startDestination = Routes.BaseScreens,
            ),
        )
        val childNavHost: @Composable () -> Unit = {
            localNavigationState.screensNavigation.setNavController(rememberNavController())
            NavHost(
                navController = localNavigationState.screensNavigation.getNavController,
                startDestination = screenDestination,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
            ) {
                screensNavGraph(resultCallbacks)
            }
        }
        NavHost(
            navController = localNavigationState.rootNavigation.getNavController,
            startDestination = Routes.BaseOrchestrated,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable<Routes.BaseOrchestrated> {
                localNavigationState.orchestratedNavigation.setNavController(
                    rememberNavController(),
                )
                Box {
                    NavHost(
                        navController =
                        localNavigationState.orchestratedNavigation.getNavController,
                        startDestination = orchestratedDestination,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) {
                        orchestratedNavGraph(childNavHost, resultCallbacks)
                    }
                }
            }
        }
    }
}

internal fun NavGraphBuilder.orchestratedNavGraph(
    content: @Composable () -> Unit,
    resultCallbacks: ResultCallbacks,
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
            showStartRoute = params.showStartRoute,
            startRoute = params.startRoute,
            onResult = { resultCallbacks.onSmartSelfieResult?.invoke(it) },
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
        OrchestratedBiometricKYCScreen(
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
            onResult = { resultCallbacks.onBiometricKYCResult?.invoke(it) },
        )
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
            resultCallbacks = resultCallbacks,
            content = content,
            userId = params.captureParams.userId,
            jobId = params.captureParams.jobId,
            showAttribution = params.captureParams.showAttribution,
            showSkipButton = params.captureParams.showSkipButton,
            allowAgentMode = params.captureParams.allowAgentMode,
            allowGalleryUpload = params.captureParams.allowGallerySelection,
            showInstructions = params.captureParams.showInstructions,
            idAspectRatio = params.captureParams.knownIdAspectRatio,
            onResult = { resultCallbacks.onDocVResult?.invoke(it) },
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
        val metadata = LocalMetadata.current
        OrchestratedDocumentVerificationScreen(
            resultCallbacks = resultCallbacks,
            content = content,
            userId = params.captureParams.userId,
            jobId = params.captureParams.jobId,
            showAttribution = params.captureParams.showAttribution,
            showSkipButton = params.captureParams.showSkipButton,
            allowAgentMode = params.captureParams.allowAgentMode,
            allowGalleryUpload = params.captureParams.allowGallerySelection,
            showInstructions = params.captureParams.showInstructions,
            idAspectRatio = params.captureParams.knownIdAspectRatio,
            onResult = { resultCallbacks.onEnhancedDocVResult?.invoke(it) },
            viewModel = viewModel(
                factory = viewModelFactory {
                    EnhancedDocumentVerificationViewModel(
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
}

internal fun NavGraphBuilder.screensNavGraph(
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
    sharedDestinations(resultCallbacks)
    selfieDestinations(resultCallbacks)
    documentsDestinations(resultCallbacks)
}

internal fun NavGraphBuilder.documentsDestinations(
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
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
            onSkip = { resultCallbacks.onDocumentInstructionSkip?.invoke() },
            onInstructionsAcknowledgedSelectFromGallery = { uri ->
                resultCallbacks.onDocumentInstructionAcknowledgedSelectFromGallery?.invoke(uri)
            },
            onInstructionsAcknowledgedTakePhoto = {
                resultCallbacks.onInstructionsAcknowledgedTakePhoto?.invoke()
            },
        )
    }

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
        val galleryDocumentUri = decodeUrl(params.galleryDocumentUri)
        DocumentCaptureScreen(
            resultCallbacks = resultCallbacks,
            jobId = params.jobId,
            side = DocumentCaptureSide.Front,
            knownIdAspectRatio = params.knownIdAspectRatio,
            galleryDocumentUri = galleryDocumentUri,
            captureTitleText = stringResource(params.captureTitleText),
            onConfirm = { file ->
                resultCallbacks.onDocumentFrontCaptureSuccess?.invoke(file)
            },
            onError = { error ->
                resultCallbacks.onDocumentCaptureError?.invoke(error)
            },
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
        val galleryDocumentUri = decodeUrl(params.galleryDocumentUri)
        DocumentCaptureScreen(
            resultCallbacks = resultCallbacks,
            jobId = params.jobId,
            side = DocumentCaptureSide.Front,
            knownIdAspectRatio = params.knownIdAspectRatio,
            galleryDocumentUri = galleryDocumentUri,
            captureTitleText = stringResource(params.captureTitleText),
            onConfirm = { file ->
                resultCallbacks.onDocumentBackCaptureSuccess?.invoke(file)
            },
            onError = { error ->
                resultCallbacks.onDocumentCaptureError?.invoke(error)
            },
            onSkip = {
                resultCallbacks.onDocumentBackSkip?.invoke()
            },
        )
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
            onInstructionsAcknowledged = {
                resultCallbacks.onSelfieInstructionScreen?.invoke()
            },
        )
    }
}

internal fun NavGraphBuilder.sharedDestinations(
    resultCallbacks: ResultCallbacks = ResultCallbacks(),
) {
    composable<Routes.Shared.ImageConfirmDialog>(
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
        selfieUrl?.let {
            val bitmap = loadBitmap(it)
            bitmap?.let { bmp ->
                ImageCaptureConfirmationDialog(
                    titleText = stringResource(params.titleText),
                    subtitleText = stringResource(params.subtitleText),
                    painter = BitmapPainter(
                        bmp,
                    ),
                    confirmButtonText = stringResource(
                        params.confirmButtonText,
                    ),
                    onConfirm = { resultCallbacks.onConfirmCapturedImage?.invoke() },
                    retakeButtonText = stringResource(
                        params.retakeButtonText,
                    ),
                    onRetake = { resultCallbacks.onImageDialogRetake?.invoke() },
                    scaleFactor = 1.25f,
                )
            }
        }
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
            onContinue = { resultCallbacks.onProcessingContinue?.invoke() },
            retryButtonText = stringResource(params.retryButtonText),
            onRetry = { resultCallbacks.onProcessingRetry?.invoke() },
            closeButtonText = stringResource(params.closeButtonText),
            onClose = { resultCallbacks.onProcessingClose?.invoke() },
        )
    }
}
