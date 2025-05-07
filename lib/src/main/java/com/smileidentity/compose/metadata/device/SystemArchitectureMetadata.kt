package com.smileidentity.compose.metadata.device

import android.os.Build

/**
 * Returns the system architecture of the device. If an error occurs, it returns "unknown".
 */
internal val systemArchitecture: String
    get() {
        return try {
            Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
        } catch (e: Throwable) {
            "unknown"
        }
    }
