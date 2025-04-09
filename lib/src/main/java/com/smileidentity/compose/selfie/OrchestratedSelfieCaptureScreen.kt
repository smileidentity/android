package com.smileidentity.compose.selfie

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.R
import com.smileidentity.compose.components.LocalMetadata
import com.smileidentity.models.v2.Metadatum
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.SelfieViewModel
import com.smileidentity.viewmodel.viewModelFactory
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * Orchestrates the selfie capture flow - navigates between instructions, requesting permissions,
 * showing camera view, and displaying processing screen
 */
@Composable
fun OrchestratedSelfieCaptureScreen(
    modifier: Modifier = Modifier,
    userId: String = rememberSaveable { randomUserId() },
    jobId: String = rememberSaveable { randomJobId() },
    allowNewEnroll: Boolean = false,
    isEnroll: Boolean = true,
    allowAgentMode: Boolean = false,
    skipApiSubmission: Boolean = false,
    showAttribution: Boolean = true,
    showInstructions: Boolean = true,
    extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    metadata: SnapshotStateList<Metadatum> = LocalMetadata.current,
    viewModel: SelfieViewModel = viewModel(
        factory = viewModelFactory {
            SelfieViewModel(
                isEnroll = isEnroll,
                userId = userId,
                jobId = jobId,
                allowNewEnroll = allowNewEnroll,
                skipApiSubmission = skipApiSubmission,
                metadata = metadata,
                extraPartnerParams = extraPartnerParams,
            )
        },
    ),
    onResult: SmileIDCallback<SmartSelfieResult> = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var acknowledgedInstructions by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .consumeWindowInsets(WindowInsets.statusBars)
            .fillMaxSize(),
    ) {
        Text("The count is ${uiState.counttt}")

        Text("The usedMemInMB is ${uiState.usedMemInMB}")

        Text("The maxHeapSizeInMB is ${uiState.maxHeapSizeInMB}")

        Text("The availHeapSizeInMB is ${uiState.availHeapSizeInMB}")

        val context = LocalContext.current
        LaunchedEffect(viewModel) {
            viewModel.juma(
                bitmap = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.batman,
                ),
            )
        }

        // SelfieCaptureScreen(
        //     userId = userId,
        //     jobId = jobId,
        //     isEnroll = isEnroll,
        //     allowAgentMode = allowAgentMode,
        //     skipApiSubmission = skipApiSubmission,
        // )

        // when {
        //     showInstructions && !acknowledgedInstructions -> SmartSelfieInstructionsScreen(
        //         showAttribution = showAttribution,
        //     ) {
        //         acknowledgedInstructions = true
        //     }
        //
        //     uiState.processingState != null -> ProcessingScreen(
        //         processingState = uiState.processingState,
        //         inProgressTitle = stringResource(R.string.si_smart_selfie_processing_title),
        //         inProgressSubtitle = stringResource(R.string.si_smart_selfie_processing_subtitle),
        //         inProgressIcon = painterResource(R.drawable.si_smart_selfie_processing_hero),
        //         successTitle = stringResource(R.string.si_smart_selfie_processing_success_title),
        //         successSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
        //             ?: stringResource(R.string.si_smart_selfie_processing_success_subtitle),
        //         successIcon = painterResource(R.drawable.si_processing_success),
        //         errorTitle = stringResource(R.string.si_smart_selfie_processing_error_title),
        //         errorSubtitle = uiState.errorMessage.resolve().takeIf { it.isNotEmpty() }
        //             ?: stringResource(id = R.string.si_processing_error_subtitle),
        //         errorIcon = painterResource(R.drawable.si_processing_error),
        //         continueButtonText = stringResource(R.string.si_continue),
        //         onContinue = { viewModel.onFinished(onResult) },
        //         retryButtonText = stringResource(R.string.si_smart_selfie_processing_retry_button),
        //         onRetry = viewModel::onRetry,
        //         closeButtonText = stringResource(R.string.si_smart_selfie_processing_close_button),
        //         onClose = { viewModel.onFinished(onResult) },
        //     )
        //
        //     // !uiState.selfieToConfirm.isNull() -> ImageCaptureConfirmationDialog(
        //     //     titleText = stringResource(R.string.si_smart_selfie_confirmation_dialog_title),
        //     //     subtitleText = stringResource(
        //     //         R.string.si_smart_selfie_confirmation_dialog_subtitle,
        //     //     ),
        //     //     painter = remember {
        //     //         val path = uiState.selfieToConfirm?.absolutePath
        //     //         try {
        //     //             BitmapFactory.decodeFile(path)?.let { bitmap: Bitmap ->
        //     //                 BitmapPainter(bitmap.asImageBitmap())
        //     //             } ?: run {
        //     //                 SmileIDCrashReporting.hub.addBreadcrumb(
        //     //                     "Failed to decode selfie image at $path",
        //     //                 )
        //     //                 ColorPainter(Color.Black)
        //     //             }
        //     //         } catch (e: Exception) {
        //     //             SmileIDCrashReporting.hub.addBreadcrumb(
        //     //                 "Error loading selfie image at $path",
        //     //             )
        //     //             SmileIDCrashReporting.hub.captureException(e)
        //     //             ColorPainter(Color.Black)
        //     //         }
        //     //     },
        //     //     confirmButtonText = stringResource(
        //     //         R.string.si_smart_selfie_confirmation_dialog_confirm_button,
        //     //     ),
        //     //     onConfirm = viewModel::submitJob,
        //     //     retakeButtonText = stringResource(
        //     //         R.string.si_smart_selfie_confirmation_dialog_retake_button,
        //     //     ),
        //     //     onRetake = viewModel::onSelfieRejected,
        //     //     scaleFactor = 1.25f,
        //     // )
        //
        //     else -> SelfieCaptureScreen(
        //         userId = userId,
        //         jobId = jobId,
        //         isEnroll = isEnroll,
        //         allowAgentMode = allowAgentMode,
        //         skipApiSubmission = skipApiSubmission,
        //     )
        // }
    }
}
