package com.smileidentity.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class SdkContext(
    @Json(name = "agent_mode") val agentMode: Boolean? = null,
    @Json(name = "allow_gallery_upload") val allowGalleryUpload: Boolean? = null,
    @Json(name = "allow_new_enroll") val allowNewEnroll: Boolean? = null,
    @Json(name = "autocapture") val autoCapture: String? = null,
    @Json(name = "api_submission_only") val apiSubmissionOnly: Boolean? = null,
    @Json(name = "bypass_selfie") val bypassSelfie: Boolean? = null,
    @Json(name = "component_mode") val componentMode: Boolean? = null,
    @Json(name = "offline_mode") val offlineMode: Boolean? = null,
    @Json(name = "show_attribution") val showAttribution: Boolean? = null,
    @Json(name = "show_instructions") val showInstructions: Boolean? = null,
    @Json(name = "skip_api_submission") val skipApiSubmission: Boolean? = null,
    @Json(name = "use_strict_mode") val useStrictMode: Boolean? = null,
) : Parcelable
