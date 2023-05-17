package com.smileidentity.compose

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.rememberAsyncImagePainter
import com.smileidentity.R
import com.smileidentity.isImageAtLeast
import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.toast
import timber.log.Timber
import java.io.File

@Composable
fun OrchestratedDocumentVerificationScreen(
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    showAttribution: Boolean = true,
    allowGalleryUpload: Boolean = false,
    enforcedIdType: Document? = null,
    idAspectRatio: Float? = enforcedIdType?.aspectRatio,
    captureBothSides: Boolean = false,
    bypassSelfieCaptureWithFile: File? = null,
    onResult: SmileIDCallback<DocumentVerificationResult> = {},
) {
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

        shouldSelectFromGallery && !isFrontDocumentPhotoValid -> {
            DocumentConfirmationScreen(
                uri = frontDocumentPhoto,
                onConfirm = { isFrontDocumentPhotoValid = true },
                onRetake = { frontDocumentPhoto = null },
            )
        }

        captureBothSides && shouldSelectFromGallery && backDocumentPhoto == null -> {
            PhotoPickerScreen { backDocumentPhoto = it }
        }

        captureBothSides && shouldSelectFromGallery && !isBackDocumentPhotoValid -> {
            DocumentConfirmationScreen(
                uri = backDocumentPhoto,
                onConfirm = { isBackDocumentPhotoValid = true },
                onRetake = { backDocumentPhoto = null },
            )
        }
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

@Composable
fun DocumentConfirmationScreen(
    uri: Uri?,
    onConfirm: () -> Unit,
    onRetake: () -> Unit,
) = ImageCaptureConfirmationDialog(
    titleText = stringResource(R.string.si_doc_v_confirmation_dialog_title),
    subtitleText = stringResource(R.string.si_doc_v_confirmation_dialog_subtitle),
    painter = rememberAsyncImagePainter(uri),
    confirmButtonText = stringResource(R.string.si_doc_v_confirmation_dialog_confirm_button),
    onConfirm = onConfirm,
    retakeButtonText = stringResource(R.string.si_doc_v_confirmation_dialog_retake_button),
    onRetake = onRetake,
)
