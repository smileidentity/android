package com.smileidentity.compose.nav

import android.os.Bundle
import androidx.navigation.NavType
import com.smileidentity.compose.components.ProcessingState
import kotlinx.serialization.json.Json

val ProcessingStateNavType = object : NavType<ProcessingState>(
    isNullableAllowed = false,
) {
    override fun put(bundle: Bundle, key: String, value: ProcessingState) {
        bundle.putParcelable(key, value)
    }
    override fun get(bundle: Bundle, key: String): ProcessingState {
        val result = bundle.getParcelable(key) as? ProcessingState
        return result ?: throw IllegalArgumentException("ProcessingState null")
    }

    override fun parseValue(value: String): ProcessingState {
        return Json.decodeFromString<ProcessingState>(value)
    }
    override val name = "DocumentCaptureTypes"
}
