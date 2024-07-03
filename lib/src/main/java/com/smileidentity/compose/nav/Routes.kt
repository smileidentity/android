package com.smileidentity.compose.nav

import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.compose.document.DocumentCaptureSide
import kotlinx.serialization.Serializable

@Serializable
object DocumentCaptureScreenInstructionRoute

@Serializable
data class DocumentCaptureScreenConfirmDocumentImageRoute(val documentImageToConfirm: String)

@Serializable
data class DocumentCaptureScreenCaptureRoute(
    val aspectRatio: Float = 1f,
    val side: DocumentCaptureSide,
    val instructionsHeroImage: Int,
    val instructionsTitleText: String,
    val instructionsSubtitleText: String,
    val captureTitleText: String,
)

@Serializable
object OrchestratedSelfieCaptureScreenRoute

@Serializable
data class OrchestratedProcessingRoute(val processingState: ProcessingState)
