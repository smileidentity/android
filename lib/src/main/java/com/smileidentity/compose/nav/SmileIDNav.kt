package com.smileidentity.compose.nav

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.smileidentity.R
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.document.DocumentCaptureScreen
import com.smileidentity.compose.document.DocumentCaptureSide
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.compose.selfie.v2.OrchestratedSelfieCaptureScreenV2
import com.smileidentity.ml.SelfieQualityModel
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
    @Serializable
    data object DocumentCaptureScreenInstructionRoute : Routes()

    @Serializable
    data class DocumentCaptureScreenConfirmDocumentImageRoute(val documentImageToConfirm: String) :
        Routes()

    @Serializable
    data class DocumentCaptureFrontRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class DocumentCaptureBackRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class SelfieCaptureScreenRoute(val params: SelfieCaptureParams) : Routes()

    @Serializable
    data class SelfieCaptureScreenRouteV2(val params: SelfieCaptureParams) : Routes()

    @Serializable
    data class OrchestratedProcessingRoute(val params: ProcessingScreenParams) : Routes()
}

fun NavGraphBuilder.mainGraph(
    navController: NavController,
    onSmartSelfieResult: SmileIDCallback<SmartSelfieResult>?,
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
}
