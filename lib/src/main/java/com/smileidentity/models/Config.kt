package com.smileidentity.models

import android.content.Context
import android.os.Parcelable
import com.smileidentity.SmileIdentity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import okio.buffer
import okio.source

/**
 * This represents the smile_config.json file that you can download from the Smile Identity portal
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Config(
    @Json(name = "partner_id") val partnerId: String,
    @Json(name = "auth_token") val authToken: String,
    @Json(name = "prod_lambda_url") val prodBaseUrl: String,
    @Json(name = "test_lambda_url") val sandboxBaseUrl: String,
) : Parcelable {
    companion object {
        fun fromAssets(context: Context): Config {
            context.assets.open("smile_config.json").source().buffer().use {
                return SmileIdentity.moshi.adapter(Config::class.java).fromJson(it)!!
            }
        }
    }
}
