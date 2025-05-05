package com.smileidentity.metadata.device

import java.util.Locale

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
