package com.smileidentity.compose.metadata.device

import java.util.TimeZone

/**
 * Returns the timezone of the device.
 */
internal val timezone: String
    get() = TimeZone.getDefault().id
