package com.smileidentity.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.JobType
import com.smileidentity.models.ProductsConfigRequest
import com.smileidentity.randomUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentSelectorUiState(
    val idTypes: Map<String, List<String>>? = null,
)

class DocumentSelectorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DocumentSelectorUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
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
            val productsConfigResponse = SmileID.api.getProductsConfig(productsConfigRequest)
            _uiState.update {
                it.copy(idTypes = productsConfigResponse.idSelection.documentVerification)
            }
        }
    }
}
