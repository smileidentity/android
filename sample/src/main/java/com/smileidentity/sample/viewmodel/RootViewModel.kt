package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.R
import com.smileidentity.sample.repo.DataStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * *****Note to Partners*****
 *
 * To enable runtime switching of the Smile Config, it is essential to have the RootViewModel.
 * For instructions on initializing the SDK, please refer to [SmileIDApplication].
 */

data class RootUiState(
    val showSmileConfigBottomSheet: Boolean = false,
    val smileConfigHint: String = "Your Smile Config from the Smile ID Portal",
    @StringRes val smileConfigError: Int? = null,
)

class RootViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RootUiState())
    val uiState = _uiState.asStateFlow()

    val runtimeConfig = DataStoreRepository.getConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    private val configAdapter = SmileID.moshi.adapter(Config::class.java)
    private val currentConfig = DataStoreRepository.getConfig()
        .map { it?.let { configAdapter.toJson(it) } ?: "Smile Config from the Smile ID Portal" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = "",
        )

    init {
        viewModelScope.launch {
            currentConfig.collect { config ->
                _uiState.update { it.copy(smileConfigHint = config) }
            }
        }
    }

    fun updateSmileConfig(updatedConfig: String) {
        try {
            val config = configAdapter.fromJson(updatedConfig)
            if (config != null) {
                viewModelScope.launch {
                    DataStoreRepository.setConfig(config)
                    _uiState.update { it.copy(showSmileConfigBottomSheet = false) }
                }
                _uiState.update { it.copy(smileConfigError = null) }
            } else {
                _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
        }
    }
}
