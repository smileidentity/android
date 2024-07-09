package com.smileidentity.compose.nav

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.compose.document.DocumentCaptureSide
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
object DocumentCaptureScreenInstructionRoute

@Serializable
data class DocumentCaptureScreenConfirmDocumentImageRoute(val documentImageToConfirm: String)

@Serializable
@Parcelize
data class DocumentCaptureScreenCaptureRoute(
    val aspectRatio: Float = 1f,
    val side: DocumentCaptureSide,
    val instructionsHeroImage: Int,
    val instructionsTitleText: String,
    val instructionsSubtitleText: String,
    val captureTitleText: String,
) : Parcelable

@Serializable
object OrchestratedSelfieCaptureScreenRoute

@Serializable
data class OrchestratedProcessingRoute(val processingState: ProcessingState)

inline fun <reified T : Parcelable> parcelableType(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key)
        }

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: T) = bundle.putParcelable(key, value)
}
