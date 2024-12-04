package com.smileidentity.viewmodel

import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.IdInfo
import com.smileidentity.models.SmileIDException
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.submissions.BiometricKYCSubmission
import com.smileidentity.submissions.base.BaseJobSubmission
import com.smileidentity.submissions.base.BaseSubmissionViewModel
import com.smileidentity.util.StringResource
import com.smileidentity.util.isNetworkFailure
import java.io.File
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class BiometricKycUiState(
    val processingState: ProcessingState? = null,
    val errorMessage: StringResource = StringResource.ResId(R.string.si_processing_error_subtitle),
)

class BiometricKycViewModel(
    private val idInfo: IdInfo,
    private val userId: String,
    private val jobId: String,
    private val allowNewEnroll: Boolean,
    private val extraPartnerParams: ImmutableMap<String, String> = persistentMapOf(),
) : BaseSubmissionViewModel<BiometricKycResult>() {
    private val _uiState = MutableStateFlow(BiometricKycUiState())
    val uiState = _uiState.asStateFlow()
    private var selfieFile: File? = null
    private var livenessFiles: List<File>? = null

    fun onSelfieCaptured(selfieFile: File, livenessFiles: List<File>) {
        this.selfieFile = selfieFile
        this.livenessFiles = livenessFiles
        submitJob()
    }

    private fun submitJob() {
        submitJob(jobId, false, SmileID.allowOfflineMode)
    }

    fun onRetry() {
        if (selfieFile == null || livenessFiles == null) {
            Timber.w("Unexpected state: Selfie or liveness files are null")
            // Set processing state to null to redirect back to selfie capture
            _uiState.update { it.copy(processingState = null) }
        } else {
            submitJob()
        }
    }

    fun onFinished(onResult: SmileIDCallback<BiometricKycResult>) = onResult(result!!)

    override fun createSubmission(): BaseJobSubmission<BiometricKycResult> {
        return BiometricKYCSubmission(
            userId,
            jobId,
            allowNewEnroll,
            livenessFiles!!,
            selfieFile!!,
            idInfo,
            extraPartnerParams,
            mutableListOf(),
        )
    }

    override fun processingState() {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
    }

    override fun handleSuccess(data: BiometricKycResult) {
        result = SmileIDResult.Success(
            data,
        )
        _uiState.update {
            it.copy(
                processingState = ProcessingState.Success,
                errorMessage = StringResource.ResId(
                    R.string.si_biometric_kyc_processing_success_subtitle,
                ),
            )
        }
    }

    override fun handleError(error: Throwable) {
        val errorMessage: StringResource = when {
            isNetworkFailure(error) -> StringResource.ResId(R.string.si_no_internet)
            error is SmileIDException -> StringResource.ResIdFromSmileIDException(error)
            else -> StringResource.ResId(R.string.si_processing_error_subtitle)
        }
        result = SmileIDResult.Error(error)
        _uiState.update {
            it.copy(
                processingState = ProcessingState.Error,
                errorMessage = errorMessage,
            )
        }
    }

    override fun handleSubmissionFiles(jobId: String) {
        TODO("Not yet implemented")
    }

    override fun handleOfflineSuccess() {
        result = SmileIDResult.Success(
            BiometricKycResult(
                selfieFile ?: throw IllegalStateException("Selfie file is null"),
                livenessFiles.orEmpty(),
                false,
            ),
        )
        _uiState.update {
            it.copy(
                processingState = ProcessingState.Success,
                errorMessage = StringResource.ResId(R.string.si_offline_message),
            )
        }
    }
}
