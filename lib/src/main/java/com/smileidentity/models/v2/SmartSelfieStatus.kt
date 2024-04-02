package com.smileidentity.models.v2

import com.serjltt.moshi.adapters.FallbackEnum
import com.squareup.moshi.Json

@Suppress("unused")
@FallbackEnum(name = "Unknown")
enum class SmartSelfieStatus {
    @Json(name = "approved")
    Approved,

    @Json(name = "pending")
    Pending,

    @Json(name = "rejected")
    Rejected,

    /**
     * Special value used to indicate that the value returned from the server is not yet supported
     * by the SDK. Please update the SDK to the latest version to support the latest values.
     */
    Unknown,
}
