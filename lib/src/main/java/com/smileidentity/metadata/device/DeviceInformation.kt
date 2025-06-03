package com.smileidentity.metadata.device

import android.os.Build
import com.smileidentity.SmileIDCrashReporting
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
            Timber.e(e)
            return false
        }
    }

/**
 * Returns the model of the device. If the device is an emulator, it returns "emulator". Any errors
 * result in "unknown"
 */
internal val model: String
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
            SmileIDCrashReporting.scopes?.addBreadcrumb("Error getting device model: $e")
            return "unknown"
        }
    }

/**
 * On Android, we return the API level, as this provides much more signal than the consumer facing
 * version number
 */
internal val os: String
    get() = "Android API ${Build.VERSION.SDK_INT}"
