package com.smileidentity.models.v2

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class FailureReason(
    @Json(name = "mobile_active_liveness_timed_out") val activeLivenessTimedOut: Boolean? = null,
) : Parcelable
