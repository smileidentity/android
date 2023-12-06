package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.R
import com.smileidentity.sample.SmileIDApplication
import com.smileidentity.sample.repo.DataStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val SMILE_CONFIG_DEFAULT_HINT = "Paste your Smile Config from the Portal here"

/**
 * *****Note to Partners*****
 *
 * To enable runtime switching of the Smile Config, it is essential to have the RootViewModel.
 * For instructions on initializing the SDK, please refer to [SmileIDApplication].
 */
data class RootUiState(
    val partnerId: String = "",
    val smileConfigHint: String = SMILE_CONFIG_DEFAULT_HINT,
    @StringRes val smileConfigError: Int? = null,
)

class RootViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RootUiState())
    val uiState = _uiState.asStateFlow()

    val runtimeConfig = DataStoreRepository.getConfig().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null,
    )

    private val configAdapter = SmileID.moshi.adapter(Config::class.java)

    fun updateSmileConfig(updatedConfig: String) {
        try {
            val config = configAdapter.fromJson(updatedConfig)
            if (config != null) {
                _uiState.update { it.copy(smileConfigError = null) }
                viewModelScope.launch {
                    DataStoreRepository.setConfig(config)
                    _uiState.update { it.copy(partnerId = config.partnerId) }
                }
            } else {
                _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
        }
    }
}
