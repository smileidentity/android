package com.smileidentity.compose.metadata.device

import android.os.Build

/**
 * On Android, we return the API level, as this provides much more signal than the consumer facing
 * version number
 */
internal val os: String
    get() = "Android API ${Build.VERSION.SDK_INT}"
