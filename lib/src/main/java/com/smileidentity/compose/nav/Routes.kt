package com.smileidentity.compose.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
    // selfie
    @Serializable
    data class SelfieCaptureScreenRoute(val params: SelfieCaptureParams) : Routes()

    @Serializable
    data class SelfieCaptureScreenRouteV2(val params: SelfieCaptureParams) : Routes()

    @Serializable
    data class SelfieInstructionsScreenRoute(val params: InstructionScreenParams) : Routes()

    @Serializable
    data class ImageCaptureConfirmDialog(val params: ImageConfirmParams) : Routes()

    // document
    @Serializable
    data class DocumentInstructionRoute(val params: DocumentInstructionParams) : Routes()

    @Serializable
    data class DocumentCaptureFrontRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class DocumentCaptureBackRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class DocumentCaptureScreenContent(val params: DocumentCaptureContentParams) : Routes()

    // shared
    @Serializable
    data class ProcessingScreenRoute(val params: ProcessingScreenParams) : Routes()

    // orchestrated
    @Serializable
    data class OrchestratedSelfieRoute(val params: SelfieCaptureParams) : Routes()

    @Serializable
    data class OrchestratedDocVRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class OrchestratedEnhancedDocVRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class OrchestratedBiometricKycRoute(val params: OrchestratedBiometricKYCParams) : Routes()
}
