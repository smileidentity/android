package com.smileidentity.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartnerParams(
    @SerialName(value = "job_type") val jobType: JobType? = null,
    @SerialName(value = "job_id") val jobId: String,
    @SerialName(value = "user_id") val userId: String,
    val extras: Map<String, String> = mapOf(),
)
