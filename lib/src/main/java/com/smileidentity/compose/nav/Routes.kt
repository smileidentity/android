package com.smileidentity.compose.nav

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
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

    @Serializable
    data class OrchestratedDocVRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class OrchestratedEnhancedDocVRoute(val params: DocumentCaptureParams) : Routes()

    @Serializable
    data class OrchestratedBiometricKycRoute(val params: OrchestratedBiometricKYCParams) : Routes()
}
