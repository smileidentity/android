package com.smileidentity.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.ProductsConfigRequest
import com.smileidentity.models.ValidDocument
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.randomUserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class DocumentSelectorUiState(
    val idTypes: ImmutableList<ValidDocument> = persistentListOf(),
    val errorMessage: String? = null,
    val hasBackSide: Boolean = false,
)

class DocumentSelectorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentSelectorUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update { it.copy(errorMessage = e.message) }
        }

        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.DocumentVerification,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val productsConfigRequest = ProductsConfigRequest(
                partnerId = SmileID.config.partnerId,
                timestamp = authResponse.timestamp,
                signature = authResponse.signature,
            )
            val response = SmileID.api.getGlobalDocVConfig(productsConfigRequest)
            _uiState.update {
                it.copy(idTypes = response.validDocuments.toPersistentList())
            }
        }
    }
}

val idTypeFriendlyNames = mapOf(
    "ALIEN_CARD" to "Alien Card",
    "BANK_ACCOUNT" to "Bank Account",
    "BVN" to "BVN",
    "CAC" to "CAC",
    "DRIVERS_LICENSE" to "Driver's License",
    "KRA_PIN" to "KRA PIN",
    "NATIONAL_ID" to "National ID",
    "NATIONAL_ID_NO_PHOTO" to "National ID (No Photo)",
    "NEW_VOTER_ID" to "New Voter ID",
    "NIN_SLIP" to "NIN Slip",
    "NIN_V2" to "NIN v2",
    "PASSPORT" to "Passport",
    "PHONE_NUMBER" to "Phone Number",
    "SSNIT" to "SSNIT",
    "TIN" to "TIN",
    "VOTER_ID" to "Voter ID",
    "V_NIN" to "Virtual NIN",
)
