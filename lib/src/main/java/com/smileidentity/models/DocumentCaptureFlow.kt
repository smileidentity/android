package com.smileidentity.models

import com.smileidentity.compose.components.ProcessingState

/**
 * Handles the navigation logic for the document orchestration screen,
 * depending on partner config and ui state
 */
internal sealed interface DocumentCaptureFlow {

    object FrontDocumentCapture : DocumentCaptureFlow
    object BackDocumentCapture : DocumentCaptureFlow

    object SelfieCapture : DocumentCaptureFlow
    data class ProcessingScreen(
        val processingState: ProcessingState,
    ) : DocumentCaptureFlow
}
