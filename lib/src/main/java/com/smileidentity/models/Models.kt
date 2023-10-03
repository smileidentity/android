@file:Suppress("unused")

package com.smileidentity.models

import android.os.Parcelable
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
@Parcelize
class SmileIDException(val details: Details) : Exception(details.message), Parcelable {

    // This Exception+Details is defined in this way to satisfy Moshi (it doesn't like data classes
    // to have parent classes - i.e. Exception as a parent class)
    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Details(
        @Json(name = "code") val code: String,
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
    val jobId: String = randomJobId(),
    val userId: String = randomUserId(),
    val extras: Map<String, String> = mapOf(),
) : Parcelable

enum class JobType(val value: Int) {
    BiometricKyc(1),
    SmartSelfieAuthentication(2),
    SmartSelfieEnrollment(4),
    EnhancedKyc(5),
    DocumentVerification(6),
    BVN(7),
    EnhancedDocV(7),

    /**
     * Special value used to indicate that the value returned from the server is not yet supported
     * by the SDK. Please update the SDK to the latest version to support the latest values.
     */
    Unknown(-1),
    ;

    companion object {
        @JvmStatic
        fun fromValue(value: Int): JobType = entries.find { it.value == value } ?: Unknown
    }
}
