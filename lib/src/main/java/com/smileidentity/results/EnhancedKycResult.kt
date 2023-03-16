package com.smileidentity.results

import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.EnhancedKycResponse

sealed interface EnhancedKycResult {
    /**
     * Enhanced KYC flow and API requests were successful
     */
    data class Success(
        val request: EnhancedKycRequest,
        val response: EnhancedKycResponse,
    ) : EnhancedKycResult

    /**
     * An error was encountered during the Enhanced KYC flow. This includes, but is not limited to,
     * network errors, API errors, and unexpected errors.
     */
    data class Error(val throwable: Throwable) : EnhancedKycResult

    // Using a Functional (SAM) interface for best Java inter-op (otherwise typealias would do)
    fun interface Callback {
        fun onResult(result: EnhancedKycResult)
    }
}
