package com.smileidentity.compose.document

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.ImageCaptureConfirmationDialog
import com.smileidentity.compose.components.ProcessingScreen
import com.smileidentity.compose.selfie.OrchestratedSelfieCaptureScreen
import com.smileidentity.models.Document
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.generateFileFromUri
import com.smileidentity.util.isImageAtLeast
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.util.toast
import com.smileidentity.viewmodel.DocumentViewModel
import com.smileidentity.viewmodel.viewModelFactory
import timber.log.Timber
import java.io.File

/**
 * Orchestrates the document capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
internal fun OrchestratedDocumentVerificationScreen(
    idType: Document,
    idAspectRatio: Float? = idType.aspectRatio,
    captureBothSides: Boolean = false,
    bypassSelfieCaptureWithFile: File? = null,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowGalleryUpload: Boolean = false,
    showInstructions: Boolean = true,
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory {
            DocumentViewModel(
                userId = userId,
                jobId = jobId,
                idType = idType,
                idAspectRatio = idAspectRatio,
            )
        },
    ),
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }
    var shouldSelectFromGallery by rememberSaveable { mutableStateOf(false) }
    var isFrontDocumentPhotoValid by rememberSaveable { mutableStateOf(false) }

    when (
        val state = DocumentCaptureFlow.stateFrom(
            acknowledgedInstructions = acknowledgedInstructions,
            processingState = uiState.processingState,
            shouldSelectFromGallery = shouldSelectFromGallery,
            captureBothSides = captureBothSides,
            isFrontDocumentPhotoValid = isFrontDocumentPhotoValid,
            showInstructions = showInstructions,
            uiState = uiState,
        )
    ) {
        DocumentCaptureFlow.ShowInstructions -> DocumentCaptureInstructionsScreen(
            title = stringResource(R.string.si_doc_v_instruction_title),
            subtitle = stringResource(id = R.string.si_verify_identity_instruction_subtitle),
            showAttribution = showAttribution,
            allowPhotoFromGallery = allowGalleryUpload,
            onInstructionsAcknowledgedTakePhoto = { acknowledgedInstructions = true },
            onInstructionsAcknowledgedSelectFromGallery = {
                acknowledgedInstructions = true
                shouldSelectFromGallery = true
            },
        )

        is DocumentCaptureFlow.ProcessingScreen -> ProcessingScreen(
            processingState = state.processingState,
            inProgressTitle = stringResource(R.string.si_doc_v_processing_title),
            inProgressSubtitle = stringResource(R.string.si_doc_v_processing_subtitle),
            inProgressIcon = painterResource(R.drawable.si_doc_v_processing_hero),
            successTitle = stringResource(R.string.si_doc_v_processing_success_title),
            successSubtitle = stringResource(R.string.si_doc_v_processing_success_subtitle),
            successIcon = painterResource(R.drawable.si_processing_success),
            errorTitle = stringResource(id = R.string.si_doc_v_processing_error_title),
            errorSubtitle = stringResource(
                uiState.errorMessage ?: R.string.si_processing_error_subtitle,
            ),
            errorIcon = painterResource(R.drawable.si_processing_error),
            continueButtonText = stringResource(
                id = R.string.si_smart_selfie_processing_continue_button,
            ),
            onContinue = { viewModel.onFinished(onResult) },
            retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
            onRetry = { viewModel.onRetry(captureBothSides) },
            closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
            onClose = { viewModel.onFinished(onResult) },
        )

        DocumentCaptureFlow.FrontDocumentCapture -> DocumentCaptureScreen(
            userId = userId,
            jobId = jobId,
            idType = idType,
            idAspectRatio = idAspectRatio,
            titleText = stringResource(id = R.string.si_doc_v_capture_instructions_front_title),
            subtitleText = stringResource(id = R.string.si_doc_v_capture_instructions_subtitle),
            bypassSelfieCaptureWithFile = bypassSelfieCaptureWithFile,
        )

        DocumentCaptureFlow.FrontDocumentCaptureConfirmation -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.frontDocumentImageToConfirm!!.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_confirm_button,
            ),
            onConfirm = {
                isFrontDocumentPhotoValid = true
            },
            retakeButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_retake_button,
            ),
            onRetake = {
                viewModel.onDocumentRejected(isBackSide = true)
                isFrontDocumentPhotoValid = false
            },
        )

        DocumentCaptureFlow.BackDocumentCapture -> DocumentCaptureScreen(
            userId = userId,
            jobId = jobId,
            idType = idType,
            idAspectRatio = idAspectRatio,
            titleText = stringResource(id = R.string.si_doc_v_capture_instructions_back_title),
            subtitleText = stringResource(id = R.string.si_doc_v_capture_instructions_subtitle),
            isBackSide = true,
            bypassSelfieCaptureWithFile = bypassSelfieCaptureWithFile,
        )

        DocumentCaptureFlow.BackDocumentCaptureConfirmation -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.backDocumentImageToConfirm!!.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_confirm_button,
            ),
            onConfirm = { viewModel.onDocumentConfirmed() },
            retakeButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_retake_button,
            ),
            onRetake = { viewModel.onDocumentRejected(isBackSide = true) },
        )

        DocumentCaptureFlow.CameraOneSide -> DocumentCaptureScreen(
            userId = userId,
            jobId = jobId,
            idType = idType,
            idAspectRatio = idAspectRatio,
            titleText = stringResource(id = R.string.si_doc_v_capture_instructions_front_title),
            subtitleText = stringResource(id = R.string.si_doc_v_capture_instructions_subtitle),
        )

        DocumentCaptureFlow.CameraOneSideConfirmation -> {
            ImageCaptureConfirmationDialog(
                titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
                subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
                painter = BitmapPainter(
                    BitmapFactory.decodeFile(uiState.frontDocumentImageToConfirm!!.absolutePath)
                        .asImageBitmap(),
                ),
                confirmButtonText = stringResource(
                    id = R.string.si_doc_v_confirmation_dialog_confirm_button,
                ),
                onConfirm = { viewModel.onDocumentConfirmed() },
                retakeButtonText = stringResource(
                    id = R.string.si_doc_v_confirmation_dialog_retake_button,
                ),
                onRetake = { viewModel.onDocumentRejected(isBackSide = false) },
            )
        }

        DocumentCaptureFlow.FrontDocumentGallerySelection -> PhotoPickerScreen {
            viewModel.saveFileFromGallerySelection(generateFileFromUri(uri = it, context = context))
        }

        DocumentCaptureFlow.FrontDocumentGalleryConfirmation -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.frontDocumentImageToConfirm!!.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_confirm_button,
            ),
            onConfirm = {
                isFrontDocumentPhotoValid = true
            },
            retakeButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_retake_button,
            ),
            onRetake = {
                viewModel.onDocumentRejected(isBackSide = false)
                isFrontDocumentPhotoValid = false
            },
        )

        DocumentCaptureFlow.BackDocumentGallerySelection -> PhotoPickerScreen {
            viewModel.saveFileFromGallerySelection(
                generateFileFromUri(uri = it, context = context),
                isBackSide = true,
            )
        }

        DocumentCaptureFlow.BackDocumentGalleryConfirmation -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.backDocumentImageToConfirm!!.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_confirm_button,
            ),
            onConfirm = { viewModel.onDocumentConfirmed() },
            retakeButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_retake_button,
            ),
            onRetake = { viewModel.onDocumentRejected(true) },
        )

        DocumentCaptureFlow.GalleryOneSide -> PhotoPickerScreen {
            viewModel.saveFileFromGallerySelection(generateFileFromUri(uri = it, context = context))
            isFrontDocumentPhotoValid = true
        }

        DocumentCaptureFlow.GalleryOneSideConfirmation -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.frontDocumentImageToConfirm!!.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_confirm_button,
            ),
            onConfirm = { viewModel.onDocumentConfirmed() },
            retakeButtonText = stringResource(
                id = R.string.si_doc_v_confirmation_dialog_retake_button,
            ),
            onRetake = { viewModel.onDocumentRejected(isBackSide = false) },
        )

        DocumentCaptureFlow.SelfieCapture -> OrchestratedSelfieCaptureScreen(
            userId = userId,
            jobId = jobId,
            isEnroll = false,
            allowAgentMode = false,
            showAttribution = showAttribution,
            showInstructions = showInstructions,
            skipApiSubmission = true,
        ) {
            when (it) {
                is SmileIDResult.Error -> viewModel.onSelfieCaptureError(it)
                is SmileIDResult.Success -> viewModel.onSelfieCaptureSuccess(it)
            }
        }

        DocumentCaptureFlow.Unknown -> {
            SmileIDCrashReporting.hub.captureException(
                IllegalStateException(
                    "Document Verification option not available \n" +
                        " values passed are : " +
                        " acknowledgedInstructions $acknowledgedInstructions " +
                        " processingState $uiState.processingState " +
                        " uiState $uiState " +
                        " shouldSelectFromGallery $shouldSelectFromGallery " +
                        " captureBothSides $captureBothSides " +
                        " isFrontDocumentPhotoValid $isFrontDocumentPhotoValid ",
                ),
            )
        }
    }
}

@Composable
fun PhotoPickerScreen(onPhotoSelected: (documentPhoto: Uri) -> Unit) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
        onResult = { uri ->
            Timber.v("selectedUri: $uri")
            uri?.let {
                if (isImageAtLeast(context, uri, width = 1920, height = 1080)) {
                    onPhotoSelected(uri)
                } else {
                    context.toast(R.string.si_doc_v_validation_image_too_small)
                }
            }
        },
    )
    LaunchedEffect(Unit) {
        photoPickerLauncher.launch(PickVisualMediaRequest(ImageOnly))
    }
}
