package com.smileidentity.models

import android.content.Context
import android.os.Parcelable
import com.smileidentity.SmileID
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.FileNotFoundException
import java.io.IOException
import kotlinx.parcelize.Parcelize
import okio.buffer
import okio.source

/**
 * This represents the smile_config.json file that you can download from the Smile ID portal
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
            try {
                context.assets.open("smile_config.json").source().buffer().use {
                    return SmileID.moshi.adapter(Config::class.java).fromJson(it)
                        ?: throw IllegalStateException("Failed to parse smile_config.json")
                }
            } catch (e: IOException) {
                throw FileNotFoundException(
                    "smile_config.json not found in assets. Please ensure the file exists.",
                )
            }
        }
    }
}
