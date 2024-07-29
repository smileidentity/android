package com.smileidentity.compose.nav

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.smileidentity.compose.components.ProcessingState
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Parcelize
data class OrchestratedSelfieCaptureScreenParams(
    val userId: String,
    val jobId: String,
    val allowNewEnroll: Boolean = false,
    val isEnroll: Boolean = true,
    val allowAgentMode: Boolean = false,
    val skipApiSubmission: Boolean = false,
    val showAttribution: Boolean = true,
    val showInstructions: Boolean = true,
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
data class DocumentCaptureScreenParams(
    val userId: String,
    val jobId: String,
    val showInstructions: Boolean,
    val showAttribution: Boolean,
    val allowGallerySelection: Boolean,
    val showSkipButton: Boolean,
    val instructionsHeroImage: Int,
    val instructionsTitleText: Int,
    val instructionsSubtitleText: Int,
    val captureTitleText: Int,
    val knownIdAspectRatio: Float?,
) : Parcelable

class CustomNavType<T : Parcelable>(
    private val clazz: Class<T>,
    private val serializer: KSerializer<T>,
) : NavType<T>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, clazz) as T
        } else {
            @Suppress("DEPRECATION") // for backwards compatibility
            bundle.getParcelable(key)
        }

    override fun parseValue(value: String): T = Json.decodeFromString(serializer, value)

    override fun put(bundle: Bundle, key: String, value: T) = bundle.putParcelable(key, value)

    override fun serializeAsValue(value: T): String = Json.encodeToString(serializer, value)

    override val name: String = clazz.name
}
