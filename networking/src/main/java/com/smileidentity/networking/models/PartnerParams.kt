package com.smileidentity.networking.models

import com.smileidentity.networking.util.randomJobId
import com.smileidentity.networking.util.randomUserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartnerParams(
    @SerialName(value = "job_type") val jobType: JobType? = null,
    @SerialName(value = "job_id") val jobId: String = randomJobId(),
    @SerialName(value = "user_id") val userId: String = randomUserId(),
    val extras: Map<String, String> = mapOf(),
)
