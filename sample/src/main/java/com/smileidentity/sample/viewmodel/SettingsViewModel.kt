package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.R
import com.smileidentity.sample.repo.DataStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val showSmileConfigBottomSheet: Boolean = false,
    val smileConfigHint: String = SMILE_CONFIG_DEFAULT_HINT,
    @StringRes val smileConfigError: Int? = null,
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()
    private val configAdapter = SmileID.moshi.adapter(Config::class.java)

    fun showSmileConfigInput() {
        _uiState.update { it.copy(showSmileConfigBottomSheet = true) }
    }

    fun hideSmileConfigInput() {
        _uiState.update { it.copy(showSmileConfigBottomSheet = false) }
    }

    fun updateSmileConfig(updatedConfig: String): Config? {
        try {
            val config = configAdapter.fromJson(updatedConfig)
            if (config != null) {
                viewModelScope.launch {
                    DataStoreRepository.setConfig(config)
                    _uiState.update { it.copy(showSmileConfigBottomSheet = false) }
                }
                _uiState.update { it.copy(smileConfigError = null) }
                return config
            } else {
                _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
                return null
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
            return null
        }
    }
}
