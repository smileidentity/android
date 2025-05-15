package com.smileidentity.models

import com.smileidentity.compose.components.ProcessingState

/**
 * Handles the navigation logic for the document orchestration screen,
 * depending on partner config and ui state
 */
internal sealed interface DocumentCaptureFlow {
    data object FrontDocumentCapture : DocumentCaptureFlow
    data object BackDocumentCapture : DocumentCaptureFlow
    data object SelfieCapture : DocumentCaptureFlow
    data class ProcessingScreen(val processingState: ProcessingState) : DocumentCaptureFlow
}
