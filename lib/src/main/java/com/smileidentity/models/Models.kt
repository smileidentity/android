@file:Suppress("unused")

package com.smileidentity.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
@Parcelize
class SmileIDException(val details: Details) : Exception(details.message), Parcelable {

    // This Exception+Details is defined in this way to satisfy Moshi (it doesn't like data classes
    // to have parent classes - i.e. Exception as a parent class)
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Details(
        @Json(name = "code") val code: Int,
        @Json(name = "error") val message: String,
    ) : Parcelable
}

/**
 * Custom values specific to partners can be placed in [extras]
 */
// The class uses a custom adapter in order to support placing the key-value pairs in [extras] into
// top level fields in the JSON
@Parcelize
data class PartnerParams(
    val jobType: JobType? = null,
    val jobId: String = UUID.randomUUID().toString(),
    val userId: String = UUID.randomUUID().toString(),
    val extras: Map<String, String> = mapOf(),
) : Parcelable

enum class JobType(val value: Int) {
    BiometricKyc(1),
    SmartSelfieAuthentication(2),
    SmartSelfieEnrollment(4),
    EnhancedKyc(5),
    DocumentVerification(6),

    /**
     * Special value used to indicate that the value returned from the server is not yet supported
     * by the SDK. Please update the SDK to the latest version to support the latest values.
     */
    Unknown(-1),
    ;

    companion object {
        @JvmStatic
        fun fromValue(value: Int): JobType = values().find { it.value == value } ?: Unknown
    }
}
