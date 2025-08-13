package com.smileidentity.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationRequest(val country: String? = null)

@Serializable
data class AuthenticationResponse(val success: Boolean)
