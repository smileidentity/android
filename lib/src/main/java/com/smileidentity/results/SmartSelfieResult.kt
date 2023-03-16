package com.smileidentity.results

import com.smileidentity.models.JobStatusResponse
import java.io.File

/**
 * The result of a SmartSelfie capture and submission to the Smile Identity API.
 */
sealed interface SmartSelfieResult {
    /**
     * Selfie capture and network requests were successful. The Job itself may or may not be
     * complete yet. This can be checked with [JobStatusResponse.jobComplete]. If not yet complete,
     * the job status will need to be fetched again later. If the job is complete, the final job
     * success can be checked with [JobStatusResponse.jobSuccess]
     */
    data class Success(
        val selfieFile: File,
        val livenessFiles: List<File>,
        val jobStatusResponse: JobStatusResponse,
    ) : SmartSelfieResult

    /**
     * An error was encountered during the SmartSelfie flow. This includes, but is not limited to,
     * denied Camera permissions, file errors, network errors, API errors, and unexpected errors.
     */
    data class Error(val throwable: Throwable) : SmartSelfieResult

    // Using a Functional (SAM) interface for best Java inter-op (otherwise typealias would do)
    fun interface Callback {
        fun onResult(result: SmartSelfieResult)
    }
}
