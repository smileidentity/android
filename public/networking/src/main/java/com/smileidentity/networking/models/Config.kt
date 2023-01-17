package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    @Json(name = "prod_lambda_url") val baseUrl: String,
    @Json(name = "test_lambda_url") val sandboxBaseUrl: String,
    @Json(name = "partner_id") val partnerId: String,
)
