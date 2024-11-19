package com.smileidentity.compose.nav

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
sealed class Routes : Parcelable {

    @Parcelize
    @Serializable
    data object Root : Routes()

    @Parcelize
    @Serializable
    data object BaseOrchestrated : Routes()

    @Parcelize
    @Serializable
    data object BaseScreens : Routes()

    sealed class Selfie : Routes() {
        @Parcelize
        @Serializable
        data class CaptureScreen(val params: SelfieCaptureParams) : Selfie()

        @Parcelize
        @Serializable
        data class CaptureScreenV2(val params: SelfieCaptureParams) : Selfie()

        @Parcelize
        @Serializable
        data class InstructionsScreen(val params: InstructionScreenParams) : Selfie()
    }

    sealed class Document : Routes() {
        @Parcelize
        @Serializable
        data object FrontInstructionScreen : Document()

        @Parcelize
        @Serializable
        data class InstructionScreen(val params: DocumentInstructionParams) : Document()

        @Parcelize
        @Serializable
        data class CaptureFrontScreen(val params: DocumentCaptureParams) : Document()

        @Parcelize
        @Serializable
        data class CaptureBackScreen(val params: DocumentCaptureParams) : Document()
    }

    sealed class Shared : Routes() {
        @Parcelize
        @Serializable
        data class ProcessingScreen(val params: ProcessingScreenParams) : Shared()

        @Parcelize
        @Serializable
        data class ImageConfirmDialog(val params: ImageConfirmParams) : Selfie()
    }

    sealed class Orchestrated : Routes() {
        @Parcelize
        @Serializable
        data class SelfieRoute(val params: OrchestratedSelfieCaptureParams) : Orchestrated()

        @Parcelize
        @Serializable
        data class DocVRoute(val params: OrchestratedDocumentParams) : Orchestrated()

        @Parcelize
        @Serializable
        data class EnhancedDocVRoute(val params: OrchestratedDocumentParams) : Orchestrated()

        @Parcelize
        @Serializable
        data class BiometricKycRoute(
            val params: OrchestratedBiometricCaptureParams,
        ) : Orchestrated()
    }
}
