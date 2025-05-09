package com.smileidentity.metadata.device

import android.os.Build
import timber.log.Timber

/**
 * Returns the system architecture of the device. If an error occurs, it returns "unknown".
 */
internal val systemArchitecture: String
    get() {
        return try {
            Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
        } catch (e: Throwable) {
            Timber.e(e)
            "unknown"
        }
    }
