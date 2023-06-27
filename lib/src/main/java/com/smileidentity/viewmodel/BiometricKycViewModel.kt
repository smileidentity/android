package com.smileidentity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.BiometricKycJobStatusResponse
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobStatusRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.SmileIDCallback
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.getExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.time.Duration.Companion.seconds

data class BiometricKycUiState(
    val showLoading: Boolean = true,
    val showConsent: Boolean = false,
    val consentDenied: Boolean = false,
    val processingState: ProcessingState? = null,
)

class BiometricKycViewModel(
    private val idInfo: IdInfo,
    private val userId: String,
    private val jobId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BiometricKycUiState())
    val uiState = _uiState.asStateFlow()

    private var result: SmileIDResult<BiometricKycResult>? = null
    private var selfieFile: File? = null
    private var livenessFiles: List<File>? = null

    init {
        // Check whether consent is required (returned in the auth smile response)
        // on error, fall back to showing consent
        val proxy = { e: Throwable ->
            Timber.w(e)
            _uiState.update { it.copy(showLoading = false, showConsent = true) }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = JobType.BiometricKyc,
                userId = userId,
                jobId = jobId,
                country = idInfo.country,
                idType = idInfo.idType,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            if (authResponse.consentInfo?.consentRequired == true) {
                _uiState.update { it.copy(showLoading = false, showConsent = true) }
            } else {
                _uiState.update { it.copy(showLoading = false, showConsent = false) }
            }
        }
    }

    fun onConsentGranted() {
        _uiState.update { it.copy(showConsent = false, consentDenied = false) }
    }

    fun onConsentDenied() {
        _uiState.update { it.copy(showConsent = false, consentDenied = true) }
    }

    fun onConsentDeniedTryAgain() {
        _uiState.update { it.copy(showConsent = true, consentDenied = false) }
    }

    fun onSelfieCaptured(selfieFile: File, livenessFiles: List<File>) {
        this.selfieFile = selfieFile
        this.livenessFiles = livenessFiles
        submitJob(selfieFile, livenessFiles)
    }

    private fun submitJob(selfieFile: File, livenessFiles: List<File>) {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            result = SmileIDResult.Error(e)
            _uiState.update { it.copy(processingState = ProcessingState.Error) }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = JobType.BiometricKyc,
                userId = userId,
                jobId = jobId,
            )

            val authResponse = SmileID.api.authenticate(authRequest)

            val prepUploadRequest = PrepUploadRequest(
                callbackUrl = "",
                partnerParams = authResponse.partnerParams,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )
            val prepUploadResponse = SmileID.api.prepUpload(prepUploadRequest)
            val livenessImagesInfo = livenessFiles.map { it.asLivenessImage() }
            val selfieImageInfo = selfieFile.asSelfieImage()
            val uploadRequest = UploadRequest(
                images = livenessImagesInfo + selfieImageInfo,
                idInfo = idInfo,
            )
            SmileID.api.upload(prepUploadResponse.uploadUrl, uploadRequest)
            Timber.d("Upload finished")
            val jobStatusRequest = JobStatusRequest(
                jobId = authResponse.partnerParams.jobId,
                userId = authResponse.partnerParams.userId,
                includeImageLinks = false,
                includeHistory = false,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
            )

            lateinit var jobStatusResponse: BiometricKycJobStatusResponse
            val jobStatusPollDelay = 1.seconds
            for (i in 1..10) {
                Timber.v("Job Status poll attempt #$i in $jobStatusPollDelay")
                delay(jobStatusPollDelay)
                jobStatusResponse = SmileID.api.getBiometricKycJobStatus(jobStatusRequest)
                Timber.v("Job Status Response: $jobStatusResponse")
                if (jobStatusResponse.jobComplete) {
                    break
                }
            }
            result = SmileIDResult.Success(
                BiometricKycResult(
                    selfieFile,
                    livenessFiles,
                    jobStatusResponse,
                ),
            )

            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onRetry() {
        if (selfieFile == null || livenessFiles == null) {
            Timber.w("Unexpected state: Selfie or liveness files are null")
            // Set processing state to null to redirect back to selfie capture
            _uiState.update { it.copy(processingState = null) }
        } else {
            submitJob(selfieFile!!, livenessFiles!!)
        }
    }

    fun onFinished(onResult: SmileIDCallback<BiometricKycResult>) = onResult(result!!)
}
