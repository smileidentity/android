package com.smileidentity.metadata.device

import android.os.Build
import com.smileidentity.SmileIDCrashReporting
import timber.log.Timber

/**
 * Returns the model of the device. Any errors result in "unknown"
 */
internal val model: String
    get() {
        try {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.contains(manufacturer, ignoreCase = true)) {
                model
            } else {
                "$manufacturer $model"
            }
        } catch (e: Exception) {
            Timber.w(e, "Error getting device model")
            SmileIDCrashReporting.scopes.addBreadcrumb("Error getting device model: $e")
            return "unknown"
        }
    }

/**
 * On Android, we return the API level, as this provides much more signal than the consumer facing
 * version number
 */
internal val os: String
    get() = "Android API ${Build.VERSION.SDK_INT}"
