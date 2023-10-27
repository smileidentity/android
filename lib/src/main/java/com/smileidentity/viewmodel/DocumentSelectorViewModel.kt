package com.smileidentity.viewmodel

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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class DocumentSelectorUiState(
    val idTypes: ImmutableList<ValidDocument> = persistentListOf(),
    val errorMessage: String? = null,
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
            val response = SmileID.api.getValidDocuments(productsConfigRequest)
            _uiState.update {
                it.copy(idTypes = response.validDocuments.toImmutableList())
            }
        }
    }
}
