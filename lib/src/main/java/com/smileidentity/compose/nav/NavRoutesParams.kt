package com.smileidentity.compose.nav

import android.os.Parcelable
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.IdInfo
import java.io.File
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
data class DocumentCaptureParams(
    val countryCode: String,
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
    val successSubtitle: Int,
    val successIcon: Int,
    val errorTitle: Int,
    val errorSubtitle: Int,
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
