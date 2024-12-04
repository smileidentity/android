package com.smileidentity.submissions.base

import android.os.Parcelable
import com.smileidentity.results.SmileIDResult

abstract class BaseSynchronousJobSubmission<T : Parcelable, R : Parcelable>(jobId: String) :
    BaseJobSubmission<T>(
        jobId,
    ) {

    override suspend fun executeApiSubmission(offlineMode: Boolean): SmileIDResult<T> {
        if (offlineMode) {
            handleOfflinePreparation()
        }

        return try {
            createSynchronousRes(getApiResponse())
        } catch (e: Throwable) {
            SmileIDResult.Error(e)
        }
    }

    override suspend fun createSuccessResult(didSubmit: Boolean): SmileIDResult.Success<T> {
        return createSynchronousRes(getApiResponse())
    }

    protected abstract suspend fun getApiResponse(): R?
    protected abstract fun createSynchronousRes(result: R?): SmileIDResult.Success<T>
}
