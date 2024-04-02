package com.smileidentity.models.v2

import com.serjltt.moshi.adapters.FallbackEnum
import com.squareup.moshi.Json

@FallbackEnum(name = "Unknown")
enum class JobType {
    @Json(name = "smart_selfie_authentication")
    SmartSelfieAuthentication,

    @Json(name = "smart_selfie_enrollment")
    SmartSelfieEnrollment,

    /**
     * Special value used to indicate that the value returned from the server is not yet supported
     * by the SDK. Please update the SDK to the latest version to support the latest values.
     */
    Unknown,
}
