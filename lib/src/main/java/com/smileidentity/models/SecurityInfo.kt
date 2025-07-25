package com.smileidentity.models

import com.smileidentity.SmileID.moshi
import com.smileidentity.security.crypto.SmileIDCryptoManager
import com.smileidentity.util.getCurrentIsoTimestamp
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.File

/**
 * This class represents security_info.json
 */
@JsonClass(generateAdapter = true)
data class SecurityInfo(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "mac") val mac: String,
    @Transient val files: List<File>? = null
) {
    companion object {
        /**
         * Generates the security_info.json content for the zip file.
         * This includes the mac and timestamp for verification.
         *
         * @param files The files from the request
         */
        fun create(files: List<File>): SecurityInfo {
            val timestamp = getCurrentIsoTimestamp()
            val mac = SmileIDCryptoManager.shared.sign(timestamp = timestamp, files = files)
            return SecurityInfo(timestamp, mac, files)
        }

        /**
         * Obfuscates the files in the SecurityInfo object by encrypting them.
         *
         * @return The SecurityInfo object
         */
        fun SecurityInfo.obfuscate(): SecurityInfo {
            files?.let {
                SmileIDCryptoManager.shared.encrypt(timestamp, it)
            }
            return this
        }

        /**
         * Encodes the SecurityInfo object to a byte array in JSON format.
         *
         * @return The encoded byte array
         */
        fun SecurityInfo.encode(): ByteArray {
            val adapter = moshi.adapter(SecurityInfo::class.java)
            return adapter.toJson(this).toByteArray(Charsets.UTF_8)
        }
    }
}
