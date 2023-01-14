package com.smileidentity.networking.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    @Json(name = "test_url") val testUrl: String,
    @Json(name = "test_lambda_url") val testLambdaUrl: String,
    @Json(name = "prod_url") val prodUrl: String,
    @Json(name = "prod_lambda_url") val prodLambdaUrl: String,
    @Json(name = "auth_token") val authToken: String,
    @Json(name = "partner_id") val partnerId: String,
    @Json(name = "version") val version: String,
)
