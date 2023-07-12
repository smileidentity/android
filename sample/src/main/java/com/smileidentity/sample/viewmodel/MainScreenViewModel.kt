package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.JobResult
import com.smileidentity.results.BiometricKycResult
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.EnhancedKycResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.sample.BottomNavigationScreen
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.jobResultMessageBuilder
import com.smileidentity.sample.model.toJob
import com.smileidentity.sample.repo.DataStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class MainScreenUiState(
    @StringRes val appBarTitle: Int = R.string.app_name,
    val isProduction: Boolean = !SmileID.useSandbox,
    val snackbarMessage: String? = null,
    val bottomNavSelection: BottomNavigationScreen = startScreen,
    val pendingJobCount: Int = 0,
    val clipboardText: AnnotatedString? = null,
) {
    @StringRes
    val environmentName = if (isProduction) R.string.production else R.string.sandbox

    companion object {
        val startScreen = BottomNavigationScreen.Home
    }
}

class MainScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState = _uiState.asStateFlow()

    val pendingJobCount = DataStoreRepository.getJobs(SmileID.config.partnerId, !SmileID.useSandbox)
        .map { it.filterNot { it.jobComplete }.size }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(),
            initialValue = 0,
        )

    fun toggleEnvironment() {
        SmileID.useSandbox = !SmileID.useSandbox
        _uiState.update { it.copy(isProduction = !SmileID.useSandbox) }
    }

    private fun onHomeSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = R.string.app_name,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    private fun onJobsSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = BottomNavigationScreen.Jobs.label,
                bottomNavSelection = BottomNavigationScreen.Jobs,
            )
        }
    }

    private fun onResourcesSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = BottomNavigationScreen.Resources.label,
                bottomNavSelection = BottomNavigationScreen.Resources,
            )
        }
    }

    private fun onAboutUsSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = BottomNavigationScreen.AboutUs.label,
                bottomNavSelection = BottomNavigationScreen.AboutUs,
            )
        }
    }

    fun onSmartSelfieEnrollmentSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.SmartSelfieEnrollment.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onSmartSelfieEnrollmentResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<SmartSelfieResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse ?: run {
                val errorMessage = "SmartSelfie Enrollment jobStatusResponse is null"
                Timber.e(errorMessage)
                _uiState.update { it.copy(snackbarMessage = errorMessage) }
                return
            }
            val actualResult = response.result as? JobResult.Entry
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Enrollment",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
                suffix = "The User ID has been copied to your clipboard",
            )
            Timber.d("$message: $result")
            _uiState.update {
                it.copy(clipboardText = AnnotatedString(userId), snackbarMessage = message)
            }
            viewModelScope.launch {
                DataStoreRepository.addJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId, true),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Enrollment error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onSmartSelfieAuthenticationSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.SmartSelfieAuthentication.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onSmartSelfieAuthenticationResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<SmartSelfieResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse ?: run {
                val errorMessage = "SmartSelfie Authentication jobStatusResponse is null"
                Timber.e(errorMessage)
                _uiState.update { it.copy(snackbarMessage = errorMessage) }
                return
            }
            val actualResult = response.result as? JobResult.Entry
            val message = jobResultMessageBuilder(
                jobName = "SmartSelfie Authentication",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            viewModelScope.launch {
                DataStoreRepository.addJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId, true),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "SmartSelfie Authentication error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onEnhancedKycSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.EnhancedKyc.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onEnhancedKycResult(result: SmileIDResult<EnhancedKycResult>) {
        if (result is SmileIDResult.Success) {
            val resultData = result.data.response
            val message = jobResultMessageBuilder(
                jobName = "Enhanced KYC",
                jobComplete = true,
                jobSuccess = true,
                code = null,
                resultCode = resultData.resultCode,
                resultText = resultData.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            viewModelScope.launch {
                DataStoreRepository.addJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = resultData.toJob(),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Enhanced KYC error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onBiometricKycSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.BiometricKyc.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onBiometricKycResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<BiometricKycResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse
            val actualResult = response.result as? JobResult.Entry
            Timber.d("Biometric KYC Result: $result")
            val message = jobResultMessageBuilder(
                jobName = "Biometric KYC",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            viewModelScope.launch {
                DataStoreRepository.addJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Biometric KYC error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun onDocumentVerificationSelected() {
        _uiState.update {
            it.copy(
                appBarTitle = ProductScreen.DocumentVerification.label,
                bottomNavSelection = BottomNavigationScreen.Home,
            )
        }
    }

    fun onDocumentVerificationResult(
        userId: String,
        jobId: String,
        result: SmileIDResult<DocumentVerificationResult>,
    ) {
        if (result is SmileIDResult.Success) {
            val response = result.data.jobStatusResponse
            val actualResult = response.result as? JobResult.Entry
            val message = jobResultMessageBuilder(
                jobName = "Document Verification",
                jobComplete = response.jobComplete,
                jobSuccess = response.jobSuccess,
                code = response.code,
                resultCode = actualResult?.resultCode,
                resultText = actualResult?.resultText,
            )
            Timber.d("$message: $result")
            _uiState.update { it.copy(snackbarMessage = message) }
            viewModelScope.launch {
                DataStoreRepository.addJob(
                    partnerId = SmileID.config.partnerId,
                    isProduction = uiState.value.isProduction,
                    job = response.toJob(userId, jobId),
                )
            }
        } else if (result is SmileIDResult.Error) {
            val th = result.throwable
            val message = "Document Verification error: ${th.message}"
            Timber.e(th, message)
            _uiState.update { it.copy(snackbarMessage = message) }
        }
    }

    fun clearJobs() {
        viewModelScope.launch {
            DataStoreRepository.clearJobs(SmileID.config.partnerId, !SmileID.useSandbox)
        }
    }

    fun onNewBottomNavSelection(it: BottomNavigationScreen) {
        when (it) {
            BottomNavigationScreen.Home -> onHomeSelected()
            BottomNavigationScreen.Jobs -> onJobsSelected()
            BottomNavigationScreen.AboutUs -> onAboutUsSelected()
            BottomNavigationScreen.Resources -> onResourcesSelected()
        }
    }
}
