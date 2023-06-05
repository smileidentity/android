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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.smileidentity.R
import com.smileidentity.compose.ImageCaptureConfirmationDialog
import com.smileidentity.isImageAtLeast
import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.toast
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
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowGalleryUpload: Boolean = false,
    enforcedIdType: Document? = null,
    idAspectRatio: Float? = enforcedIdType?.aspectRatio,
    captureBothSides: Boolean = false,
    bypassSelfieCaptureWithFile: File? = null,
    viewModel: DocumentViewModel = viewModel(
        factory = viewModelFactory {
            DocumentViewModel(
                userId = userId,
                jobId = jobId,
                enforcedIdType = enforcedIdType,
                idAspectRatio = idAspectRatio,
            )
        },
    ),
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }
    var shouldSelectFromGallery by rememberSaveable { mutableStateOf(false) }
    var frontDocumentPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isFrontDocumentPhotoValid by rememberSaveable { mutableStateOf(false) }
    var backDocumentPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isBackDocumentPhotoValid by rememberSaveable { mutableStateOf(false) }

    when {
        !acknowledgedInstructions -> {
            DocumentCaptureInstructionsScreen(
                title = stringResource(R.string.si_doc_v_instruction_title),
                subtitle = stringResource(id = R.string.si_verify_identity_instruction_subtitle),
                showAttribution = showAttribution,
                allowPhotoFromGallery = allowGalleryUpload,
                onInstructionsAcknowledgedTakePhoto = {
                    acknowledgedInstructions = true
                },
                onInstructionsAcknowledgedSelectFromGallery = {
                    acknowledgedInstructions = true
                    shouldSelectFromGallery = true
                },
            )
        }

        shouldSelectFromGallery && frontDocumentPhoto == null -> {
            PhotoPickerScreen { frontDocumentPhoto = it }
        }

        shouldSelectFromGallery && !isFrontDocumentPhotoValid -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = rememberAsyncImagePainter(frontDocumentPhoto),
            confirmButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_confirm_button),
            onConfirm = { viewModel.submitJob() },
            retakeButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_retake_button),
            onRetake = { viewModel.onDocumentRejected(isBackSide = false) },
        )

        captureBothSides && shouldSelectFromGallery && backDocumentPhoto == null -> {
            PhotoPickerScreen { backDocumentPhoto = it }
        }

        captureBothSides && shouldSelectFromGallery && !isBackDocumentPhotoValid -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = rememberAsyncImagePainter(backDocumentPhoto),
            confirmButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_confirm_button),
            onConfirm = { viewModel.submitJob() },
            retakeButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_retake_button),
            onRetake = { viewModel.onDocumentRejected(isBackSide = true) },
        )

        !shouldSelectFromGallery && uiState.frontDocumentImageToConfirm == null -> DocumentCaptureScreen(
            userId = userId,
            jobId = jobId,
            enforcedIdType = enforcedIdType,
            idAspectRatio = idAspectRatio,
            titleText = stringResource(id = R.string.si_doc_v_capture_instructions_front_title),
            subtitleText = stringResource(id = R.string.si_doc_v_capture_instructions_subtitle),
        )

        uiState.frontDocumentImageToConfirm != null -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.frontDocumentImageToConfirm.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_confirm_button),
            onConfirm = { viewModel.submitJob() },
            retakeButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_retake_button),
            onRetake = { viewModel.onDocumentRejected(isBackSide = false) },
        )

        captureBothSides && !shouldSelectFromGallery && uiState.backDocumentImageToConfirm == null -> DocumentCaptureScreen(
            userId = userId,
            jobId = jobId,
            enforcedIdType = enforcedIdType,
            idAspectRatio = idAspectRatio,
            titleText = stringResource(id = R.string.si_doc_v_capture_instructions_front_title),
            subtitleText = stringResource(id = R.string.si_doc_v_capture_instructions_subtitle),
            isBackSide = true,
        )

        uiState.backDocumentImageToConfirm != null -> ImageCaptureConfirmationDialog(
            titleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_title),
            subtitleText = stringResource(id = R.string.si_doc_v_confirmation_dialog_subtitle),
            painter = BitmapPainter(
                BitmapFactory.decodeFile(uiState.backDocumentImageToConfirm.absolutePath)
                    .asImageBitmap(),
            ),
            confirmButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_confirm_button),
            onConfirm = { viewModel.submitJob() },
            retakeButtonText = stringResource(id = R.string.si_doc_v_confirmation_dialog_retake_button),
            onRetake = { viewModel.onDocumentRejected(isBackSide = true) },
        )
    }
}

@Composable
fun PhotoPickerScreen(onPhotoSelected: (documentPhoto: Uri) -> Unit) {
    val context = LocalContext.current
    val frontDocumentPhotoPickerLauncher = rememberLauncherForActivityResult(
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
        frontDocumentPhotoPickerLauncher.launch(PickVisualMediaRequest(ImageOnly))
    }
}
