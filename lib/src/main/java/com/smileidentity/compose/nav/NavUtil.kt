package com.smileidentity.compose.nav

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import java.io.File
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

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

internal fun getDocumentCaptureRoute(
    countryCode: String,
    params: DocumentCaptureParams,
    documentType: String?,
    captureBothSides: Boolean,
    idAspectRatio: Float?,
    bypassSelfieCaptureWithFile: File?,
    allowGalleryUpload: Boolean,
): Routes {
    val serializableFile = bypassSelfieCaptureWithFile?.let { SerializableFile.fromFile(it) }
    return Routes.OrchestratedDocVRoute(
        DocumentCaptureParams(
            userId = params.userId,
            jobId = params.jobId,
            showInstructions = params.showInstructions,
            showAttribution = params.showAttribution,
            allowAgentMode = params.allowAgentMode,
            allowGallerySelection = allowGalleryUpload,
            showSkipButton = false,
            instructionsHeroImage = 0,
            instructionsTitleText = 0,
            instructionsSubtitleText = 0,
            captureTitleText = 0,
            knownIdAspectRatio = idAspectRatio,
            allowNewEnroll = params.allowNewEnroll,
            countryCode = countryCode,
            documentType = documentType,
            captureBothSides = captureBothSides,
            selfieFile = serializableFile,
            extraPartnerParams = params.extraPartnerParams,
        ),
    )
}

internal fun getSelfieCaptureRoute(
    useStrictMode: Boolean,
    params: SelfieCaptureParams,
    isEnroll: Boolean,
): Routes {
    val captureParams = SelfieCaptureParams(
        userId = params.userId,
        jobId = params.jobId,
        allowNewEnroll = params.allowNewEnroll,
        isEnroll = isEnroll,
        allowAgentMode = params.allowAgentMode,
        showAttribution = params.showAttribution,
        showInstructions = params.showInstructions,
        useStrictMode = useStrictMode,
        extraPartnerParams = params.extraPartnerParams,
    )
    return if (useStrictMode) {
        Routes.SelfieCaptureScreenRouteV2(captureParams)
    } else {
        Routes.SelfieCaptureScreenRoute(captureParams)
    }
}
