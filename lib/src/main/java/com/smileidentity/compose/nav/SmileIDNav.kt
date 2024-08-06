package com.smileidentity.compose.nav

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.components.SmileThemeSurface
import com.smileidentity.compose.document.DocumentCaptureScreen
import com.smileidentity.compose.document.DocumentCaptureSide
import com.smileidentity.compose.document.OrchestratedDocumentVerificationScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.compose.selfie.v2.OrchestratedSelfieCaptureScreenV2
import com.smileidentity.compose.theme.colorScheme
import com.smileidentity.compose.theme.typography
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.models.JobType
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.viewmodel.document.DocumentVerificationViewModel
import com.smileidentity.viewmodel.document.EnhancedDocumentVerificationViewModel
import com.smileidentity.viewmodel.viewModelFactory
import kotlin.reflect.typeOf

@Composable
internal fun BaseSmileIDScreen(
    startDestination: Routes,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = SmileID.colorScheme,
    typography: Typography = SmileID.typography,
    content: NavGraphBuilder.() -> Unit,
) {
    SmileThemeSurface(colorScheme = colorScheme, typography = typography) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
        ) {
            content()
        }
    }
}

internal fun NavGraphBuilder.mainGraph(
    onSmartSelfieResult: SmileIDCallback<SmartSelfieResult>? = null,
    onDocVResult: SmileIDCallback<DocumentVerificationResult>? = null,
    onEnhancedDocVResult: SmileIDCallback<EnhancedDocumentVerificationResult>? = null,
    onBiometricKYCResult: SmileIDCallback<BiometricKycResult>? = null,
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
        OrchestratedSelfieCaptureScreen(
            userId = params.userId,
            jobId = params.jobId,
            allowAgentMode = params.allowAgentMode,
            showAttribution = params.showAttribution,
            showInstructions = params.showInstructions,
            skipApiSubmission = params.skipApiSubmission,
            onResult = onSmartSelfieResult ?: {},
        )
    }
    composable<Routes.SelfieCaptureScreenRouteV2>(
        typeMap = mapOf(
            typeOf<SelfieCaptureParams>() to CustomNavType(
                SelfieCaptureParams::class.java,
                SelfieCaptureParams.serializer(),
            ),
        ),
    ) { navBackStackEntry ->
        val route = navBackStackEntry.toRoute<Routes.SelfieCaptureScreenRoute>()
        val params = route.params
        val context = LocalContext.current
        val selfieQualityModel = remember { SelfieQualityModel.newInstance(context) }
        OrchestratedSelfieCaptureScreenV2(
            userId = params.userId,
            allowNewEnroll = params.allowNewEnroll,
            isEnroll = params.isEnroll,
            allowAgentMode = params.allowAgentMode,
            showAttribution = params.showAttribution,
            useStrictMode = params.useStrictMode,
            selfieQualityModel = selfieQualityModel,
            onResult = onSmartSelfieResult ?: {},
        )
    }
    composable<Routes.OrchestratedProcessingRoute>(
        typeMap = mapOf(
            typeOf<ProcessingScreenParams>() to CustomNavType(
                ProcessingScreenParams::class.java,
                ProcessingScreenParams.serializer(),
            ),
        ),
    ) { backStackEntry ->
        val route: Routes.OrchestratedProcessingRoute = backStackEntry.toRoute()
        val params = route.params
        ProcessingScreen(
            processingState = params.processingState,
            inProgressTitle = stringResource(params.inProgressTitle),
            inProgressSubtitle = stringResource(params.inProgressSubtitle),
            inProgressIcon = painterResource(R.drawable.si_doc_v_processing_hero),
            successTitle = stringResource(params.successTitle),
            successSubtitle = stringResource(params.successSubtitle),
            successIcon = painterResource(R.drawable.si_processing_success),
            errorTitle = stringResource(params.errorTitle),
            errorSubtitle = stringResource(params.errorSubtitle),
            errorIcon = painterResource(R.drawable.si_processing_error),
            continueButtonText = stringResource(params.continueButtonText),
            onContinue = { /*viewModel.onFinished(onResult)*/ },
            retryButtonText = stringResource(params.retryButtonText),
            onRetry = { /*viewModel::onRetry*/ },
            closeButtonText = stringResource(params.closeButtonText),
            onClose = { /*viewModel.onFinished(onResult)*/ },
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
            onConfirm = { /*viewModel::onDocumentFrontCaptureSuccess */ },
            onError = { /* viewModel::onError*/ },
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
            onConfirm = { /*viewModel::onDocumentFrontCaptureSuccess */ },
            onError = { /* viewModel::onError*/ },
        )
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
        val metadata = LocalMetadata.current
        val selfieFile = params.selfieFile?.toFile()
        OrchestratedDocumentVerificationScreen(
            userId = params.userId,
            jobId = params.jobId,
            showAttribution = params.showAttribution,
            allowAgentMode = params.allowAgentMode,
            allowGalleryUpload = params.allowGallerySelection,
            showInstructions = params.showInstructions,
            idAspectRatio = params.knownIdAspectRatio,
            onResult = onDocVResult ?: {},
            viewModel = viewModel(
                factory = viewModelFactory {
                    DocumentVerificationViewModel(
                        jobType = JobType.DocumentVerification,
                        userId = params.userId,
                        jobId = params.jobId,
                        allowNewEnroll = params.allowNewEnroll,
                        countryCode = params.countryCode,
                        documentType = params.documentType,
                        captureBothSides = params.captureBothSides,
                        selfieFile = selfieFile,
                        extraPartnerParams = params.extraPartnerParams,
                        metadata = metadata,
                    )
                },
            ),
        )
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
        val metadata = LocalMetadata.current
        val selfieFile = params.selfieFile?.toFile()
        OrchestratedDocumentVerificationScreen(
            userId = params.userId,
            jobId = params.jobId,
            showAttribution = params.showAttribution,
            allowAgentMode = params.allowAgentMode,
            allowGalleryUpload = params.allowGallerySelection,
            showInstructions = params.showInstructions,
            idAspectRatio = params.knownIdAspectRatio,
            onResult = onEnhancedDocVResult ?: {},
            viewModel = viewModel(
                factory = viewModelFactory {
                    EnhancedDocumentVerificationViewModel(
                        jobType = JobType.DocumentVerification,
                        userId = params.userId,
                        jobId = params.jobId,
                        allowNewEnroll = params.allowNewEnroll,
                        countryCode = params.countryCode,
                        documentType = params.documentType,
                        captureBothSides = params.captureBothSides,
                        selfieFile = selfieFile,
                        extraPartnerParams = params.extraPartnerParams,
                        metadata = metadata,
                    )
                },
            ),
        )
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
        val metadata = LocalMetadata.current
        val selfieFile = params.selfieFile?.toFile()
        OrchestratedDocumentVerificationScreen(
            userId = params.userId,
            jobId = params.jobId,
            showAttribution = params.showAttribution,
            allowAgentMode = params.allowAgentMode,
            allowGalleryUpload = params.allowGallerySelection,
            showInstructions = params.showInstructions,
            idAspectRatio = params.knownIdAspectRatio,
            onResult = onEnhancedDocVResult ?: {},
            viewModel = viewModel(
                factory = viewModelFactory {
                    EnhancedDocumentVerificationViewModel(
                        jobType = JobType.DocumentVerification,
                        userId = params.userId,
                        jobId = params.jobId,
                        allowNewEnroll = params.allowNewEnroll,
                        countryCode = params.countryCode,
                        documentType = params.documentType,
                        captureBothSides = params.captureBothSides,
                        selfieFile = selfieFile,
                        extraPartnerParams = params.extraPartnerParams,
                        metadata = metadata,
                    )
                },
            ),
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
        OrchestratedBiometricKYCScreen(
            idInfo = params.idInfo,
            userId = params.userId,
            jobId = params.jobId,
            allowNewEnroll = params.allowNewEnroll,
            allowAgentMode = params.allowAgentMode,
            showAttribution = params.showAttribution,
            showInstructions = params.showInstructions,
            extraPartnerParams = params.extraPartnerParams,
            onResult = onBiometricKYCResult ?: {},
        )
    }
}
