package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.Config
import com.smileidentity.sample.R
import com.smileidentity.sample.SmileIDApplication
import com.smileidentity.sample.data.mapper.toModel
import com.smileidentity.sample.data.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    val showSmileConfigConfirmation: Boolean = false,
)

@HiltViewModel
class RootViewModel @Inject constructor(
    private val repository: ConfigRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RootUiState())
    val uiState = _uiState.asStateFlow()

    val runtimeConfig = repository.fetchConfigs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null,
    )

    private val configAdapter = SmileID.moshi.adapter(Config::class.java)
    private var pendingConfig: Config? = null

    fun updateSmileConfig(updatedConfig: String) {
        try {
            val config = configAdapter.fromJson(updatedConfig)
            if (config != null) {
                _uiState.update { it.copy(smileConfigError = null) }
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(partnerId = config.partnerId, showSmileConfigConfirmation = true)
                    }
                    pendingConfig = config
                }
            } else {
                _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
        }
    }

    fun onConfirmationContinue() {
        try {
            pendingConfig?.let { config ->
                viewModelScope.launch {
                    repository.createConfig(configModel = config.toModel())
                }
            }
        } catch (e: Exception) {
            pendingConfig = null
            _uiState.update { it.copy(smileConfigError = R.string.settings_smile_config_error) }
        }
    }
}
