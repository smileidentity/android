package com.smileidentity.compose.nav

import android.os.Parcelable
import androidx.camera.core.ImageProxy
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.IdInfo
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedDocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.viewmodel.BiometricKycViewModel
import com.smileidentity.viewmodel.SelfieViewModel
import com.smileidentity.viewmodel.document.DocumentVerificationViewModel
import com.smileidentity.viewmodel.document.EnhancedDocumentVerificationViewModel
import com.ujizin.camposer.state.CameraState
import java.io.File
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

internal data class ResultCallbacks(
    // selfie
    var onSelfieInstructionScreen: (() -> Unit)? = null,

    // shared
    var onImageDialogRetake: (() -> Unit)? = null,
    var onConfirmCapturedImage: (() -> Unit)? = null,
    var onProcessingContinue: (() -> Unit)? = null,
    var onProcessingRetry: (() -> Unit)? = null,
    var onProcessingClose: (() -> Unit)? = null,

    // document
    var onDocumentFrontCaptureSuccess: ((File) -> Unit)? = null,
    var onDocumentBackCaptureSuccess: ((File) -> Unit)? = null,
    var onDocumentCaptureError: ((Throwable) -> Unit)? = null,
    var onDocumentInstructionSkip: (() -> Unit)? = null,
    var onDocumentBackSkip: (() -> Unit)? = null,
    var onDocumentInstructionAcknowledgedSelectFromGallery: (() -> Unit)? = null,
    var onInstructionsAcknowledgedTakePhoto: (() -> Unit)? = null,

    var onCaptureClicked: ((CameraState) -> Unit)? = null,
    var imageAnalyzer: ((ImageProxy, CameraState) -> Unit)? = null,
    var onFocusEvent: ((Int) -> Unit)? = null,

    // results
    var onSmartSelfieResult: SmileIDCallback<SmartSelfieResult>? = null,
    var onDocVResult: SmileIDCallback<DocumentVerificationResult>? = null,
    var onEnhancedDocVResult: SmileIDCallback<EnhancedDocumentVerificationResult>? = null,
    var onBiometricKYCResult: SmileIDCallback<BiometricKycResult>? = null,

    // view models
    var selfieViewModel: SelfieViewModel? = null,
    var documentViewModel: DocumentVerificationViewModel? = null,
    var enhancedDocVViewModel: EnhancedDocumentVerificationViewModel? = null,
    var biometricKycViewModel: BiometricKycViewModel? = null,
)

@Serializable
@Parcelize
data class SelfieCaptureParams(
    val userId: String,
    val jobId: String,
    val allowNewEnroll: Boolean = false,
    val allowAgentMode: Boolean = false,
    val showAttribution: Boolean = true,
    val showInstructions: Boolean = true,
    val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    val isEnroll: Boolean = true,
    val skipApiSubmission: Boolean = false,
    val useStrictMode: Boolean = true,
) : Parcelable

@Serializable
@Parcelize
data class InstructionScreenParams(
    val showAttribution: Boolean = false,
) : Parcelable

@Serializable
@Parcelize
data class ImageConfirmParams(
    val titleText: Int,
    val subtitleText: Int,
    val imageFilePath: String,
    val confirmButtonText: Int,
    val retakeButtonText: Int,
    val scaleFactor: Float,
) : Parcelable

@Serializable
@Parcelize
data class DocumentCaptureParams(
    val userId: String,
    val jobId: String,
    val allowNewEnroll: Boolean = false,
    val allowAgentMode: Boolean = false,
    val showAttribution: Boolean = true,
    val showInstructions: Boolean = true,
    val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
    val allowGallerySelection: Boolean = false,
    val showSkipButton: Boolean = false,
    val instructionsHeroImage: Int = 0,
    val instructionsTitleText: Int = 0,
    val instructionsSubtitleText: Int = 0,
    val captureTitleText: Int = 0,
    val knownIdAspectRatio: Float? = null,
    val documentType: String? = null,
    val captureBothSides: Boolean = true,
    val selfieFile: SerializableFile? = null,
    val countryCode: String? = null,
) : Parcelable

@Serializable
@Parcelize
data class DocumentCaptureContentParams(
    val titleText: Int,
    val subtitleText: Int,
    val idAspectRatio: Float = 1.59f,
    val areEdgesDetected: Boolean = true,
    val showCaptureInProgress: Boolean = false,
    val showManualCaptureButton: Boolean = true,
) : Parcelable

@Serializable
@Parcelize
data class DocumentInstructionParams(
    val heroImage: Int,
    val title: String,
    val subtitle: String,
    val showAttribution: Boolean = true,
    val allowPhotoFromGallery: Boolean = false,
    val showSkipButton: Boolean = true,
) : Parcelable

@Serializable
@Parcelize
data class OrchestratedBiometricKYCParams(
    val idInfo: IdInfo,
    val userId: String,
    val jobId: String,
    val allowNewEnroll: Boolean = false,
    val allowAgentMode: Boolean = false,
    val showAttribution: Boolean = true,
    val showInstructions: Boolean = true,
    val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : Parcelable

@Serializable
@Parcelize
data class ProcessingScreenParams(
    val processingState: ProcessingState,
    val inProgressTitle: Int,
    val inProgressSubtitle: Int,
    val inProgressIcon: Int,
    val successTitle: Int,
    val successSubtitle: String,
    val successIcon: Int,
    val errorTitle: Int,
    val errorSubtitle: String,
    val errorIcon: Int,
    val continueButtonText: Int,
    val retryButtonText: Int,
    val closeButtonText: Int,
) : Parcelable

@Serializable
@Parcelize
data class SerializableFile(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val lastModified: Long,
) : Parcelable {
    fun toFile(): File = File(path)

    companion object {
        fun fromFile(file: File): SerializableFile = SerializableFile(
            path = file.absolutePath,
            name = file.name,
            isDirectory = file.isDirectory,
            lastModified = file.lastModified(),
        )
    }
}
