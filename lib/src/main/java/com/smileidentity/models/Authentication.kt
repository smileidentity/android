package com.smileidentity.models

import android.os.Parcelable
import com.smileidentity.SmileID
import com.smileidentity.models.JobType.SmartSelfieEnrollment
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * The Auth Smile request. Auth Smile serves multiple purposes:
 * - It is used to fetch the signature needed for subsequent API requests
 * - It indicates the type of job that will being performed
 * - It is used to fetch consent information for the partner
 *
 * @param jobType The type of job that will be performed
 * @param enrollment Whether or not this is an enrollment job
 * @param country The country code of the country where the job is being performed. This value is
 * required in order to get back consent information for the partner
 * @param idType The type of ID that will be used for the job. This value is required in order to
 * get back consent information for the partner
 * @param updateEnrolledImage Whether or not the enrolled image should be updated with image
 * submitted for this job
 * @param jobId The job ID to associate with the job. Most often, this will correspond to a unique
 * Job ID within your own system. If not provided, a random job ID will be generated
 * @param userId The user ID to associate with the job. Most often, this will correspond to a unique
 * User ID within your own system. If not provided, a random user ID will be generated
 * @param signature Whether or not to fetch the signature for the job
 * @param production Whether or not to use the production environment
 * @param partnerId The partner ID
 * @param authToken The auth token from smile_config.json
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class AuthenticationRequest(
    @Json(name = "job_type") val jobType: JobType? = null,
    @Json(name = "enrollment") val enrollment: Boolean = jobType == SmartSelfieEnrollment,
    @Json(name = "country") val country: String? = null,
    @Json(name = "id_type") val idType: String? = null,
    @Json(name = "update_enrolled_image") val updateEnrolledImage: Boolean? = null,
    @Json(name = "job_id") val jobId: String? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "signature") val signature: Boolean = true,
    @Json(name = "production") val production: Boolean = !SmileID.useSandbox,
    @Json(name = "partner_id") val partnerId: String = SmileID.config.partnerId,
    // we'd like to remove this when writing to the file system in offline mode
    @Json(name = "auth_token") var authToken: String = SmileID.config.authToken,
) : Parcelable

/**
 * [consentInfo] is only populated when a country and ID type are provided in the
 * [AuthenticationRequest]. To get information about *all* countries and ID types instead, use
 * [com.smileidentity.networking.SmileIDService.getProductsConfig]
 *
 * [timestamp] is *not* a [java.util.Date] because technically, any arbitrary value could have been
 * passed to it. This applies to all other timestamp fields in the SDK.
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class AuthenticationResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "signature") val signature: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "policy") val policy: Int? = null,
    @Json(name = "partner_params") val partnerParams: PartnerParams,
    @Json(name = "callback_url") val callbackUrl: String? = SmileID.callbackUrl,
    @Json(name = "consent_info") val consentInfo: ConsentInfo? = null,
) : Parcelable

/**
 * @param canAccess Whether or not the ID type is enabled for the partner
 * @param consentRequired Whether or not consent is required for the ID type
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class ConsentInfo(
    @Json(name = "can_access") val canAccess: Boolean,
    @Json(name = "consent_required") val consentRequired: Boolean,
) : Parcelable
