package com.smileidentity.networking.models

import com.smileidentity.networking.serializer.JobTypeSerializer
import kotlinx.serialization.Serializable

@Serializable(with = JobTypeSerializer::class)
enum class JobType(val value: Int) {
    BiometricKyc(1),
    SmartSelfieAuthentication(2),
    SmartSelfieEnrollment(4),
    EnhancedKyc(5),
    DocumentVerification(6),
    BVN(7),
    EnhancedDocumentVerification(11),

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
