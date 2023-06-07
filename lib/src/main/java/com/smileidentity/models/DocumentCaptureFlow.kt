package com.smileidentity.models

import com.smileidentity.compose.ProcessingState
import com.smileidentity.viewmodel.DocumentUiState

/**
 * Handles the navigation logic for the document orchestration screen,
 * depending on partner config and ui state
 */
internal sealed class DocumentCaptureFlow {

    object AcknowledgedInstructions : DocumentCaptureFlow()

    data class ProcessingScreen(
        val processingState: ProcessingState,
    ) : DocumentCaptureFlow()

    object GalleryOneSide : DocumentCaptureFlow()

    object GalleryOneSideConfirmation : DocumentCaptureFlow()

    object GalleryBothSides : DocumentCaptureFlow()

    object GalleryBothSidesConfirmation : DocumentCaptureFlow()

    object GalleryBothSidesBack : DocumentCaptureFlow()

    object GalleryBothSidesBackConfirmation : DocumentCaptureFlow()

    object CameraOneSide : DocumentCaptureFlow()

    object CameraOneSideConfirmation : DocumentCaptureFlow()

    object CameraBothSides : DocumentCaptureFlow()

    object CameraBothSidesBack : DocumentCaptureFlow()

    object CameraBothSidesConfirmation : DocumentCaptureFlow()

    object CameraBothSidesBackConfirmation : DocumentCaptureFlow()

    object UnknownDocumentCaptureFlowOption : DocumentCaptureFlow()

    companion object {
        fun documentCaptureType(
            acknowledgedInstructions: Boolean,
            processingState: ProcessingState?,
            uiState: DocumentUiState?,
            shouldSelectFromGallery: Boolean,
            captureBothSides: Boolean,
            isFrontDocumentPhotoValid: Boolean,
            isBackDocumentPhotoValid: Boolean,
        ): DocumentCaptureFlow {
            val selectGalleryOneSide =
                shouldSelectFromGallery && !captureBothSides && !isFrontDocumentPhotoValid
            val selectGalleryOneSideConfirmation =
                shouldSelectFromGallery && !captureBothSides && isFrontDocumentPhotoValid
            val selectGalleryTwoSides =
                shouldSelectFromGallery && captureBothSides && !isFrontDocumentPhotoValid
            val selectGalleryTwoSidesConfirmation =
                shouldSelectFromGallery && captureBothSides && isFrontDocumentPhotoValid
            val selectGalleryTwoSidesBack =
                shouldSelectFromGallery && captureBothSides && isFrontDocumentPhotoValid && !isBackDocumentPhotoValid
            val selectGalleryTwoSidesBackConfirmation =
                shouldSelectFromGallery && captureBothSides && isFrontDocumentPhotoValid && isBackDocumentPhotoValid
            val captureOneSideCamera =
                !shouldSelectFromGallery && !captureBothSides && uiState?.frontDocumentImageToConfirm == null
            val captureOneSideCameraConfirmation =
                !shouldSelectFromGallery && !captureBothSides && uiState?.frontDocumentImageToConfirm != null
            val captureTwoSidesCamera =
                !shouldSelectFromGallery && captureBothSides && uiState?.frontDocumentImageToConfirm == null
            val captureTwoSidesCameraConfirmation =
                !shouldSelectFromGallery && captureBothSides && !isFrontDocumentPhotoValid && uiState?.frontDocumentImageToConfirm != null
            val captureTwoSidesCameraBack =
                !shouldSelectFromGallery && captureBothSides && isFrontDocumentPhotoValid && uiState?.backDocumentImageToConfirm == null
            val captureTwoSidesCameraBackConfirmation =
                !shouldSelectFromGallery && captureBothSides && isFrontDocumentPhotoValid && uiState?.backDocumentImageToConfirm != null

            return when {
                !acknowledgedInstructions -> AcknowledgedInstructions
                processingState != null -> ProcessingScreen(processingState = processingState)
                selectGalleryOneSide -> GalleryOneSide
                selectGalleryOneSideConfirmation -> GalleryOneSideConfirmation
                selectGalleryTwoSides -> GalleryBothSides
                selectGalleryTwoSidesBack -> GalleryBothSidesBack
                selectGalleryTwoSidesConfirmation -> GalleryBothSidesConfirmation
                selectGalleryTwoSidesBackConfirmation -> GalleryBothSidesBackConfirmation
                captureOneSideCamera -> CameraOneSide
                captureOneSideCameraConfirmation -> CameraOneSideConfirmation
                captureTwoSidesCamera -> CameraBothSides
                captureTwoSidesCameraConfirmation -> CameraBothSidesConfirmation
                captureTwoSidesCameraBack -> CameraBothSidesBack
                captureTwoSidesCameraBackConfirmation -> CameraBothSidesBackConfirmation
                else -> UnknownDocumentCaptureFlowOption
            }
        }
    }
}
