package com.smileidentity.compose.document

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.compose.nav.ResultCallbacks
import com.smileidentity.compose.nav.localNavigationState
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
    resultCallbacks: ResultCallbacks,
    jobId: String,
    side: DocumentCaptureSide,
    captureTitleText: String,
    onConfirm: (File) -> Unit,
    onError: (Throwable) -> Unit,
    knownIdAspectRatio: Float?,
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
    resultCallbacks.onDocumentInstructionAcknowledgedSelectFromGallery = {
        Timber.v("onInstructionsAcknowledgedSelectFromGallery")
        SmileIDCrashReporting.hub.addBreadcrumb("Selecting document photo from gallery")
        photoPickerLauncher.launch(PickVisualMediaRequest(ImageOnly))
    }
    resultCallbacks.onInstructionsAcknowledgedTakePhoto = {
        viewModel.onInstructionsAcknowledged()
        localNavigationState.screensNavigation.getNavController.popBackStack()
    }
    resultCallbacks.onDocumentInstructionSkip = onSkip
    val aspectRatio by animateFloatAsState(
        targetValue = uiState.idAspectRatio,
        label = "ID Aspect Ratio",
    )
    when {
        documentImageToConfirm != null -> viewModel.onConfirm(documentImageToConfirm, onConfirm)
        else -> {
            CaptureScreenContent(
                titleText = captureTitleText,
                subtitleText = stringResource(id = uiState.directive.displayText),
                idAspectRatio = aspectRatio,
                areEdgesDetected = uiState.areEdgesDetected,
                showCaptureInProgress = uiState.showCaptureInProgress,
                showManualCaptureButton = uiState.showManualCaptureButton,
                onCaptureClicked = viewModel::captureDocumentManually,
                imageAnalyzer = viewModel::analyze,
                onFocusEvent = viewModel::onFocusEvent,
                modifier = modifier,
            )
        }
    }
}
