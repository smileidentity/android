package com.smileidentity.models

import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.viewmodel.DocumentUiState

/**
 * Handles the navigation logic for the document orchestration screen,
 * depending on partner config and ui state
 */
internal sealed class DocumentCaptureFlow {

    object ShowInstructions : DocumentCaptureFlow()

    data class ProcessingScreen(
        val processingState: ProcessingState,
    ) : DocumentCaptureFlow()

    object GalleryOneSide : DocumentCaptureFlow()

    object GalleryOneSideConfirmation : DocumentCaptureFlow()

    object FrontDocumentGallerySelection : DocumentCaptureFlow()

    object FrontDocumentGalleryConfirmation : DocumentCaptureFlow()

    object BackDocumentGallerySelection : DocumentCaptureFlow()

    object BackDocumentGalleryConfirmation : DocumentCaptureFlow()

    object CameraOneSide : DocumentCaptureFlow()

    object CameraOneSideConfirmation : DocumentCaptureFlow()

    object FrontDocumentCapture : DocumentCaptureFlow()

    object BackDocumentCapture : DocumentCaptureFlow()

    object FrontDocumentCaptureConfirmation : DocumentCaptureFlow()

    object BackDocumentCaptureConfirmation : DocumentCaptureFlow()

    object SelfieCapture : DocumentCaptureFlow()

    object Unknown : DocumentCaptureFlow()

    companion object {
        fun stateFrom(
            processingState: ProcessingState?,
            uiState: DocumentUiState?,
            shouldSelectFromGallery: Boolean,
            captureBothSides: Boolean,
            isFrontDocumentPhotoValid: Boolean,
            showCaptureWithInstructions: Boolean,
        ): DocumentCaptureFlow {
            val selectGalleryOneSide =
                shouldSelectFromGallery && !captureBothSides &&
                    uiState?.frontDocumentImageToConfirm == null
            val selectGalleryOneSideConfirmation =
                shouldSelectFromGallery && !captureBothSides &&
                    uiState?.frontDocumentImageToConfirm != null

            val selectGalleryTwoSides =
                shouldSelectFromGallery && captureBothSides &&
                    uiState?.frontDocumentImageToConfirm == null
            val selectGalleryTwoSidesConfirmation =
                shouldSelectFromGallery && captureBothSides &&
                    !isFrontDocumentPhotoValid && uiState?.frontDocumentImageToConfirm != null

            val selectGalleryTwoSidesBack =
                shouldSelectFromGallery && captureBothSides &&
                    isFrontDocumentPhotoValid && uiState?.backDocumentImageToConfirm == null

            val selectGalleryTwoSidesBackConfirmation =
                shouldSelectFromGallery && captureBothSides &&
                    isFrontDocumentPhotoValid && uiState?.backDocumentImageToConfirm != null

            val captureOneSideCamera =
                !shouldSelectFromGallery && !captureBothSides &&
                    uiState?.frontDocumentImageToConfirm == null
            val captureOneSideCameraConfirmation =
                !shouldSelectFromGallery && !captureBothSides &&
                    uiState?.frontDocumentImageToConfirm != null

            val captureTwoSidesCamera =
                !shouldSelectFromGallery && captureBothSides &&
                    uiState?.frontDocumentImageToConfirm == null
            val captureTwoSidesCameraConfirmation =
                !shouldSelectFromGallery && captureBothSides &&
                    !isFrontDocumentPhotoValid && uiState?.frontDocumentImageToConfirm != null

            val captureTwoSidesCameraBack =
                !shouldSelectFromGallery && captureBothSides &&
                    isFrontDocumentPhotoValid && uiState?.backDocumentImageToConfirm == null
            val captureTwoSidesCameraBackConfirmation =
                !shouldSelectFromGallery && captureBothSides &&
                    isFrontDocumentPhotoValid && uiState?.backDocumentImageToConfirm != null

            val showSelfieCapture = uiState?.showSelfieCapture ?: false

            return when {
                showCaptureWithInstructions -> ShowInstructions
                processingState != null -> ProcessingScreen(processingState = processingState)
                showSelfieCapture -> SelfieCapture
                selectGalleryOneSide -> GalleryOneSide
                selectGalleryOneSideConfirmation -> GalleryOneSideConfirmation
                selectGalleryTwoSides -> FrontDocumentGallerySelection
                selectGalleryTwoSidesBack -> BackDocumentGallerySelection
                selectGalleryTwoSidesConfirmation -> FrontDocumentGalleryConfirmation
                selectGalleryTwoSidesBackConfirmation -> BackDocumentGalleryConfirmation
                captureOneSideCamera -> CameraOneSide
                captureOneSideCameraConfirmation -> CameraOneSideConfirmation
                captureTwoSidesCamera -> FrontDocumentCapture
                captureTwoSidesCameraConfirmation -> FrontDocumentCaptureConfirmation
                captureTwoSidesCameraBack -> BackDocumentCapture
                captureTwoSidesCameraBackConfirmation -> BackDocumentCaptureConfirmation
                else -> Unknown
            }
        }
    }
}
