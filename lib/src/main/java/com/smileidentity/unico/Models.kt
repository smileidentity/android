package com.smileidentity.unico

// Data classes for API communication
data class PersonInfo(
    val duiType: String,
    val duiValue: String,
    val friendlyName: String
)

data class CreateProcessRequest(
    val callbackUri: String,
    val flow: String,
    val person: PersonInfo,
    val purpose: String
)

data class CreateProcessResponse(
    val processId: String,
    val status: String,
    val webLink: String? = null,
    val createdAt: String? = null
)

data class TokenResponse(
    val tokenType: String,
    val accessToken: String,
    val expiresIn: Int
)

// UI State
data class UnicoProcessState(
    val isLoading: Boolean = false,
    val webLink: String? = null,
    val processId: String? = null,
    val error: String? = null,
    val isProcessComplete: Boolean = false,
    val progress: Int = 0
)
