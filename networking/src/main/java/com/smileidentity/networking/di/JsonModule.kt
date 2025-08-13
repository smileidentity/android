package com.smileidentity.networking.di

import kotlinx.serialization.json.Json

/**
 * Create the [Json] instance used by the SDK.
 */
internal val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}
