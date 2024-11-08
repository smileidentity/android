package com.smileidentity.compose.nav

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import com.smileidentity.R
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import timber.log.Timber

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

@Composable
internal fun getDocumentCaptureRoute(params: DocumentCaptureParams): Routes {
    return if (params.showInstructions) {
        Routes.Document.InstructionScreen(
            params = DocumentInstructionParams(
                R.drawable.si_doc_v_front_hero,
                stringResource(R.string.si_doc_v_instruction_title),
                stringResource(R.string.si_verify_identity_instruction_subtitle),
                params.showAttribution,
                params.allowGallerySelection,
                showSkipButton = false,
            ),
        )
    } else {
        Routes.Document.CaptureFrontScreen(
            DocumentCaptureParams(
                userId = params.userId,
                jobId = params.jobId,
                showInstructions = true,
                showAttribution = params.showAttribution,
                allowAgentMode = params.allowAgentMode,
                allowGallerySelection = params.allowGallerySelection,
                showSkipButton = params.showSkipButton,
                instructionsHeroImage = params.instructionsHeroImage,
                instructionsTitleText = params.instructionsTitleText,
                instructionsSubtitleText = params.instructionsSubtitleText,
                captureTitleText = params.captureTitleText,
                knownIdAspectRatio = params.knownIdAspectRatio,
                allowNewEnroll = params.allowNewEnroll,
                countryCode = params.countryCode,
                documentType = params.documentType,
                captureBothSides = params.captureBothSides,
                selfieFile = params.selfieFile,
                extraPartnerParams = params.extraPartnerParams,
            ),
        )
    }
}

internal fun getSelfieCaptureRoute(useStrictMode: Boolean, params: SelfieCaptureParams): Routes {
    return if (params.showInstructions) {
        Routes.Selfie.InstructionsScreen(
            InstructionScreenParams(params.showAttribution),
        )
    } else if (useStrictMode) {
        Routes.Selfie.CaptureScreenV2(params)
    } else {
        Routes.Selfie.CaptureScreen(params)
    }
}

fun encodeUrl(url: String): String {
    return URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
}

internal fun decodeUrl(encodedUrl: String?): String? {
    return encodedUrl?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
}

internal fun loadBitmap(url: String) = try {
    BitmapFactory.decodeFile(url)?.asImageBitmap()
} catch (e: Exception) {
    Timber.e("ImageConfirmDialog", "Error decoding bitmap: ${e.message}", e)
    null
}

internal fun compareRouteStrings(
    routeClass: Routes?,
    currentDestination: NavDestination?,
): Boolean {
    routeClass?.let {
        val clazz = routeClass::class
        val routeName = clazz.simpleName ?: return false

        currentDestination?.route?.let { route ->
            val destinationName = route.split(".")
                .lastOrNull()?.split("/")?.firstOrNull() ?: return false
            return routeName == destinationName
        } ?: return false
    } ?: return false
}
