package com.smileidentity.submissions.base

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.handleOfflineJobFailure
import com.smileidentity.util.isNetworkFailure
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

abstract class BaseSubmissionViewModel<T : Parcelable> : ViewModel() {

    protected var result: SmileIDResult<T>? = null

    protected abstract fun createSubmission(): BaseJobSubmission<T>
    protected abstract fun processingState()
    protected abstract fun handleSuccess(data: T)
    protected abstract fun handleError(error: Throwable)
    protected abstract fun handleSubmissionFiles(jobId: String)
    protected abstract fun handleOfflineSuccess()

    protected val proxyErrorHandler = { jobId: String, error: Throwable ->
        val didMoveToSubmitted = handleOfflineJobFailure(jobId, error)
        if (didMoveToSubmitted) {
            handleSubmissionFiles(jobId)
        }

        when {
            SmileID.allowOfflineMode && isNetworkFailure(error) -> {
                handleOfflineSuccess()
            }

            else -> handleError(error)
        }
    }

    protected fun submitJob(
        jobId: String,
        skipApiSubmission: Boolean = false,
        offlineMode: Boolean = SmileID.allowOfflineMode,
    ) {
        processingState()

        viewModelScope.launch(
            getExceptionHandler { error ->
                proxyErrorHandler(jobId, error)
            },
        ) {
            val submission = createSubmission()
            when (
                val submissionResult =
                    submission.executeSubmission(skipApiSubmission, offlineMode)
            ) {
                is SmileIDResult.Success -> {
                    result = submissionResult
                    handleSuccess(submissionResult.data)
                }

                is SmileIDResult.Error -> {
                    result = submissionResult
                    proxyErrorHandler(jobId, submissionResult.throwable)
                }
            }
        }
    }

    private fun getExceptionHandler(onError: (Throwable) -> Unit): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            onError(throwable)
        }
}
