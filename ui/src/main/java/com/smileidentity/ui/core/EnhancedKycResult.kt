package com.smileidentity.ui.core

import com.smileidentity.networking.models.EnhancedKycRequest
import com.smileidentity.networking.models.EnhancedKycResponse

sealed interface EnhancedKycResult {
    /**
     * Enhanced KYC flow and API requests were successful
     */
    data class Success(
        val sessionId: String,
        val request: EnhancedKycRequest,
        val response: EnhancedKycResponse,
    ) : EnhancedKycResult

    /**
     * An error was encountered during the Enhanced KYC flow. This includes, but is not limited to,
     * network errors, API errors, and unexpected errors.
     */
    data class Error(val sessionId: String, val throwable: Throwable) : EnhancedKycResult

    // Using a Functional (SAM) interface for best Java inter-op (otherwise typealias would do)
    fun interface Callback {
        fun onResult(result: EnhancedKycResult)
    }
}
