package com.smileidentity.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The Auth Smile request. Auth Smile serves multiple purposes:
 * - It is used to fetch the signature needed for subsequent API requests
 * - It indicates the type of job that will being performed
 * - It is used to fetch consent information for the partner
 *
 * @param jobType The type of job that will be performed
 * @param enrollment Whether this is an enrollment job
 * @param country The country code of the country where the job is being performed. This value is
 * required in order to get back consent information for the partner
 * @param idType The type of ID that will be used for the job. This value is required in order to
 * get back consent information for the partner
 * @param updateEnrolledImage Whether the enrolled image should be updated with image
 * submitted for this job
 * @param jobId The job ID to associate with the job. Most often, this will correspond to a unique
 * Job ID within your own system. If not provided, a random job ID will be generated
 * @param userId The user ID to associate with the job. Most often, this will correspond to a unique
 * User ID within your own system. If not provided, a random user ID will be generated
 * @param signature Whether to fetch the signature for the job
 * @param production Whether to use the production environment
 * @param partnerId The partner ID
 * @param authToken The auth token
 */
@Serializable
data class AuthenticationRequest(
    @SerialName(value = "job_type") val jobType: JobType? = null,
    val enrollment: Boolean = jobType == JobType.SmartSelfieEnrollment,
    val country: String? = null,
    @SerialName(value = "id_type") val idType: String? = null,
    @SerialName(value = "update_enrolled_image") val updateEnrolledImage: Boolean? = null,
    @SerialName(value = "job_id") val jobId: String? = null,
    @SerialName(value = "user_id") val userId: String? = null,
    val signature: Boolean = true,
    val production: Boolean,
    @SerialName(value = "partner_id") val partnerId: String,
    @SerialName(value = "auth_token") val authToken: String,
)

/**
 * [timestamp] is *not* a [java.util.Date] because technically, any arbitrary value could have been
 * passed to it. This applies to all other timestamp fields in the SDK.
 * [consentInfo] is only populated when a country and ID type are provided in the
 */
@Serializable
data class AuthenticationResponse(
    val success: Boolean,
    val signature: String,
    val timestamp: String,
    @SerialName(value = "partner_params") val partnerParams: PartnerParams,
    @SerialName(value = "callback_url") val callbackUrl: String?,
    @SerialName(value = "consent_info") val consentInfo: ConsentInfo? = null,
)
