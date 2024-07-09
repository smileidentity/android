package com.smileidentity.compose.nav

import android.os.Bundle
import androidx.navigation.NavType
import com.smileidentity.compose.document.DocumentCaptureSide
import kotlinx.serialization.json.Json

val DocumentCaptureSideNavType = object : NavType<DocumentCaptureSide>(
    isNullableAllowed = false,
) {
    override fun put(bundle: Bundle, key: String, value: DocumentCaptureSide) {
        bundle.putParcelable(key, value)
    }
    override fun get(bundle: Bundle, key: String): DocumentCaptureSide {
        val result = bundle.getParcelable(key) as? DocumentCaptureSide
        return result ?: throw IllegalArgumentException("DocumentCaptureSide null")
    }

    override fun parseValue(value: String): DocumentCaptureSide {
        return Json.decodeFromString<DocumentCaptureSide>(value)
    }
    override val name = "DocumentCaptureTypes"
}
