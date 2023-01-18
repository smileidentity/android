package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This represents the smile_config.json file that you can download from the Smile Identity portal
 */
@JsonClass(generateAdapter = true)
data class Config(
    @Json(name = "partner_id") val partnerId: String,
    @Json(name = "auth_token") val authToken: String,
    @Json(name = "prod_lambda_url") val prodBaseUrl: String,
    @Json(name = "test_lambda_url") val sandboxBaseUrl: String,
) {
    /**
     * Companion Object declared so that it can be used as a hook to define a custom initializer for
     * Android (loading smile_config.json from an application's assets)
     */
    companion object
}
