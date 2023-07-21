package com.smileidentity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.BvnToptRequest
import com.smileidentity.models.BvnVerificationMode
import com.smileidentity.models.JobType
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.randomUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

internal data class BvnConsentUiState(
    val showLoading: Boolean = false,
    val showWrongBvn: Boolean = false,
    val bvnVerificationSuccess: Boolean = false,
    val bvnVerificationModes: List<BvnVerificationMode> = listOf(),
)

private const val NIGERIA = "NG"
private const val NIGERIA_BVN = "BVN_MFA"

internal class BvnConsentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BvnConsentUiState())
    val uiState = _uiState.asStateFlow()

    fun requestBvnOtpMode(bvnNumber: String) {
        _uiState.update { it.copy(showLoading = true) }
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update {
                it.copy(
                    showWrongBvn = true,
                    showLoading = false,
                )
            }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BVN,// TODO - What JobType?
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val bvnToptRequest = BvnToptRequest(
                country = NIGERIA,
                idNumber = bvnNumber,
                idType = NIGERIA_BVN,
                signature = authResponse.signature, // TODO - So do we need to do auth or not?
                timestamp = authResponse.timestamp,
            )
            val response = SmileID.api.requestBvnOtpMode(request = bvnToptRequest)
            _uiState.update {
                it.copy(
                    showLoading = false,
                    showWrongBvn = false,
                    bvnVerificationSuccess = true,
                    bvnVerificationModes = response.modes,
                )
            }
        }
    }

}