package com.smileidentity.compose.document

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smileidentity.R
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.nav.DocumentCaptureContentParams
import com.smileidentity.compose.nav.DocumentInstructionParams
import com.smileidentity.compose.nav.ImageConfirmParams
import com.smileidentity.compose.nav.ResultCallbacks
import com.smileidentity.compose.nav.Routes
import com.smileidentity.compose.nav.encodeUrl
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.util.createDocumentFile
import com.smileidentity.util.isValidDocumentImage
import com.smileidentity.util.toast
import com.smileidentity.util.writeUriToFile
import com.smileidentity.viewmodel.document.DocumentCaptureViewModel
import com.smileidentity.viewmodel.viewModelFactory
import java.io.File
import timber.log.Timber

const val PREVIEW_SCALE_FACTOR = 1.1f

enum class DocumentCaptureSide {
    Front,
    Back,
}

/**
 * This handles Instructions + Capture + Confirmation for a single side of a document
 */
@Composable
internal fun DocumentCaptureScreen(
    navController: NavController,
    resultCallbacks: ResultCallbacks,
    jobId: String,
    side: DocumentCaptureSide,
    showInstructions: Boolean,
    showAttribution: Boolean,
    allowGallerySelection: Boolean,
    showSkipButton: Boolean,
    @DrawableRes instructionsHeroImage: Int,
    instructionsTitleText: String,
    instructionsSubtitleText: String,
    captureTitleText: String,
    knownIdAspectRatio: Float?,
    onConfirm: (File) -> Unit,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    metadata: SnapshotStateList<Metadatum> = LocalMetadata.current,
    onSkip: () -> Unit = { },
    viewModel: DocumentCaptureViewModel = viewModel(
        factory = viewModelFactory {
            DocumentCaptureViewModel(
                jobId,
                side,
                knownIdAspectRatio,
                metadata,
            )
        },
        key = side.name,
    ),
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Timber.v("selectedUri: $uri")
            if (uri == null) {
                Timber.e("selectedUri is null")
                context.toast(R.string.si_doc_v_capture_error_subtitle)
                return@rememberLauncherForActivityResult
            }
            if (isValidDocumentImage(context, uri)) {
                val documentFile = createDocumentFile(jobId, (side == DocumentCaptureSide.Front))
                writeUriToFile(documentFile, uri, context)
                viewModel.onPhotoSelectedFromGallery(documentFile)
            } else {
                SmileIDCrashReporting.hub.addBreadcrumb("Gallery upload document image too small")
                context.toast(R.string.si_doc_v_validation_image_too_small)
            }
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val documentImageToConfirm = uiState.documentImageToConfirm
    val captureError = uiState.captureError
    resultCallbacks.onDocumentInstructionAcknowledgedSelectFromGallery = {
        Timber.v("onInstructionsAcknowledgedSelectFromGallery")
        SmileIDCrashReporting.hub.addBreadcrumb("Selecting document photo from gallery")
        photoPickerLauncher.launch(PickVisualMediaRequest(ImageOnly))
    }
    resultCallbacks.onInstructionsAcknowledgedTakePhoto = {
        navController.navigate(
            Routes.Document.CaptureScreenContent(
                DocumentCaptureContentParams(
                    titleText = R.string.si_doc_v_instruction_title,
                    subtitleText = uiState.directive.displayText,
                    idAspectRatio = knownIdAspectRatio ?: 1.59f,
                    areEdgesDetected = uiState.areEdgesDetected,
                    showCaptureInProgress = uiState.showCaptureInProgress,
                    showManualCaptureButton = uiState.areEdgesDetected,
                ),
            ),
        )
    }
    resultCallbacks.onCaptureClicked = viewModel::captureDocumentManually
    resultCallbacks.imageAnalyzer = viewModel::analyze
    resultCallbacks.onFocusEvent = viewModel::onFocusEvent
    resultCallbacks.onDocumentInstructionSkip = onSkip
    documentImageToConfirm?.let {
        navController.navigate(
            Routes.Shared.ImageConfirmDialog(
                ImageConfirmParams(
                    titleText = R.string.si_smart_selfie_confirmation_dialog_title,
                    subtitleText = R.string.si_smart_selfie_confirmation_dialog_subtitle,
                    imageFilePath = encodeUrl(documentImageToConfirm.absolutePath),
                    confirmButtonText = R.string.si_doc_v_confirmation_dialog_confirm_button,
                    retakeButtonText = R.string.si_doc_v_confirmation_dialog_retake_button,
                    scaleFactor = 1.0f,
                ),
            ),
        )
        // resultCallbacks.onConfirmCapturedImage = { viewModel.onConfirm(documentImageToConfirm, onConfirm) }
        // resultCallbacks.onImageDialogRetake = viewModel::onRetry
    }
    when {
        captureError != null -> onError(captureError)
        showInstructions && !uiState.acknowledgedInstructions -> {
            navController.navigate(
                Routes.Document.InstructionScreen(
                    DocumentInstructionParams(
                        heroImage = instructionsHeroImage,
                        title = instructionsTitleText,
                        subtitle = instructionsSubtitleText,
                        showAttribution = showAttribution,
                        allowPhotoFromGallery = allowGallerySelection,
                        showSkipButton = showSkipButton,
                    ),
                ),
            )
        }

        documentImageToConfirm != null -> {
            navController.navigate(
                Routes.Shared.ImageConfirmDialog(
                    ImageConfirmParams(
                        titleText = R.string.si_smart_selfie_confirmation_dialog_title,
                        subtitleText = R.string.si_smart_selfie_confirmation_dialog_subtitle,
                        imageFilePath = encodeUrl(documentImageToConfirm.absolutePath),
                        confirmButtonText = R.string.si_doc_v_confirmation_dialog_confirm_button,
                        retakeButtonText = R.string.si_doc_v_confirmation_dialog_retake_button,
                        scaleFactor = 1.0f,
                    ),
                ),
            )
        }

        else -> {
            // val aspectRatio by animateFloatAsState(
            //     targetValue = uiState.idAspectRatio,
            //     label = "ID Aspect Ratio",
            // )
            // CaptureScreenContent(
            //     titleText = captureTitleText,
            //     subtitleText = stringResource(id = uiState.directive.displayText),
            //     idAspectRatio = aspectRatio,
            //     areEdgesDetected = uiState.areEdgesDetected,
            //     showCaptureInProgress = uiState.showCaptureInProgress,
            //     showManualCaptureButton = uiState.showManualCaptureButton,
            //     onCaptureClicked = viewModel::captureDocumentManually,
            //     imageAnalyzer = viewModel::analyze,
            //     onFocusEvent = viewModel::onFocusEvent,
            //     modifier = modifier,
            // )
        }
    }
}
