package com.smileidentity.compose.metadata.device

import android.os.Build
import com.smileidentity.SmileIDCrashReporting
import timber.log.Timber

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
            SmileIDCrashReporting.hub.addBreadcrumb("Error getting device model: $e")
            return "unknown"
        }
    }
