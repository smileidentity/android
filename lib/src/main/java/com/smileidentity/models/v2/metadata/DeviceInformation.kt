package com.smileidentity.models.v2.metadata

import android.os.Build
import com.smileidentity.SmileIDCrashReporting
import java.util.Locale
import java.util.TimeZone
import timber.log.Timber

private val isEmulator: Boolean
    get() {
        try {
            return (
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.PRODUCT.contains("sdk_google") ||
                    Build.PRODUCT.contains("google_sdk") ||
                    Build.PRODUCT.contains("sdk") ||
                    Build.PRODUCT.contains("sdk_x86") ||
                    Build.PRODUCT.contains("sdk_gphone64_arm64") ||
                    Build.PRODUCT.contains("vbox86p") ||
                    Build.PRODUCT.contains("emulator") ||
                    Build.PRODUCT.contains("simulator")
                )
        } catch (e: Exception) {
            return false
        }
    }

/**
 * Returns the model of the device. If the device is an emulator, it returns "emulator". Any errors
 * result in "unknown"
 */
val model: String
    get() {
        try {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (isEmulator) {
                "emulator"
            } else if (model.contains(manufacturer, ignoreCase = true)) {
                model
            } else {
                "$manufacturer $model"
            }
        } catch (e: Exception) {
            Timber.w(e, "Error getting device model")
            SmileIDCrashReporting.hub.addBreadcrumb("Error getting device model: $e")
            return "unknown"
        }
    }

/**
 * On Android, we return the API level, as this provides much more signal than the consumer facing
 * version number
 */
internal val os: String
    get() = "Android API ${Build.VERSION.SDK_INT}"

/**
 * Returns the timezone of the device.
 */
internal val timezone: String
    get() = TimeZone.getDefault().id

/**
 * Returns the locale of the device in the format "language_country". If an error occurs, it returns
 * "unknown".
 */
internal val locale: String
    get() {
        return try {
            val locale = Locale.getDefault()
            "${locale.language}_${locale.country}"
        } catch (e: Exception) {
            "unknown"
        }
    }

/**
 * Returns the system architecture of the device. If an error occurs, it returns "unknown".
 */
internal val systemArchitecture: String
    get() {
        val abis = Build.SUPPORTED_ABIS
        return abis.firstOrNull() ?: "unknown"
    }
