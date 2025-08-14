package com.smileidentity.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConsentInfo(
    @SerialName(value = "can_access") val canAccess: Boolean,
    @SerialName(value = "consent_required") val consentRequired: Boolean,
)
