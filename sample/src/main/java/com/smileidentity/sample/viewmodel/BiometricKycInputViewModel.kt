package com.smileidentity.sample.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AvailableIdType
import com.smileidentity.models.IdInfo
import com.smileidentity.models.IdTypes
import com.smileidentity.models.JobType
import com.smileidentity.models.ProductsConfigRequest
import com.smileidentity.models.ServicesResponse
import com.smileidentity.sample.compose.SearchableInputFieldItem
import com.smileidentity.sample.countryDetails
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.randomUserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class BiometricKycInputUiState(
    val countries: ImmutableList<SearchableInputFieldItem>? = null,
    val selectedCountry: SearchableInputFieldItem? = null,
    val idTypesForCountry: List<AvailableIdType>? = null,
    val selectedIdType: AvailableIdType? = null,
    val hasIdTypeSelectionBeenConfirmed: Boolean = false,
    val idInputFields: List<InputFieldUi>? = null,
    val idInputFieldValues: SnapshotStateMap<String, String> = mutableStateMapOf(),
    val errorMessage: String? = null,
) {
    val isIdTypeContinueEnabled
        get() = selectedCountry != null && selectedIdType != null

    /**
     * NB! This is *not* guarded on whether the Regex validations have been satisfied -- this is
     * in case the Regex is incorrect/invalid/out-of-date, so that the user can still proceed.
     */
    val isFinalContinueEnabled
        get() = isIdTypeContinueEnabled && idInputFieldValues.values.all { it.isNotBlank() }
}

class BiometricKycInputViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BiometricKycInputUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var servicesResponse: ServicesResponse
    private lateinit var supportedCountriesAndIdTypes: IdTypes

    init {
        val proxy = { e: Throwable ->
            Timber.e(e)
            _uiState.update { it.copy(errorMessage = e.message) }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            // Use Products Config to get only ID types enabled for the Partner
            val authRequest = AuthenticationRequest(
                userId = randomUserId(),
                jobType = JobType.BiometricKyc,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val productsConfigRequest = ProductsConfigRequest(
                partnerId = SmileID.config.partnerId,
                timestamp = authResponse.timestamp,
                signature = authResponse.signature,
            )
            val productsConfigResponse = SmileID.api.getProductsConfig(productsConfigRequest)
            // Use Services endpoint to get the required input fields for each ID Type as well as
            // the display names
            servicesResponse = SmileID.api.getServices()

            supportedCountriesAndIdTypes = productsConfigResponse.idSelection.biometricKyc
            val countryList = servicesResponse.hostedWeb.biometricKyc
                .filter { it.countryCode in supportedCountriesAndIdTypes }
                .map {
                    // If we fall back, we will not have emoji
                    countryDetails[it.countryCode] ?: SearchableInputFieldItem(
                        it.countryCode,
                        it.name,
                    )
                }
                .sortedBy { it.displayName }
                .toImmutableList()

            _uiState.update { it.copy(countries = countryList) }
        }
    }

    fun onCountrySelected(selectedCountry: SearchableInputFieldItem) {
        val availableIdTypes = servicesResponse.hostedWeb.biometricKyc
            .first { it.countryCode == selectedCountry.key }
            .availableIdTypes
        _uiState.update {
            it.copy(
                selectedCountry = selectedCountry,
                selectedIdType = null,
                idTypesForCountry = availableIdTypes,
                idInputFieldValues = mutableStateMapOf(),
                idInputFields = null,
            )
        }
    }

    fun onIdTypeSelected(idType: AvailableIdType) {
        // Remove all the default fields
        val ignoredFields = listOf("country", "id_type", "user_id", "job_id")
        val idInputFields = (idType.requiredFields - ignoredFields).map {
            var inputFieldDetail = inputFieldDetails[it] ?: InputFieldUi(
                key = it,
                label = it,
                type = InputFieldUi.Type.Text,
            )
            idType.idNumberRegex?.let { regex ->
                if (it == "id_number") {
                    Timber.v("Regex is $regex for $it")
                    inputFieldDetail = inputFieldDetail.copy(regex = Regex(regex))
                }
            }
            return@map inputFieldDetail
        }

        _uiState.update {
            it.copy(
                selectedIdType = idType,
                idInputFields = idInputFields,
                idInputFieldValues = mutableStateMapOf<String, String>().apply {
                    idInputFields.forEach { field -> set(key = field.key, value = "") }
                },
            )
        }
    }

    fun onIdTypeConfirmed() {
        _uiState.update { it.copy(hasIdTypeSelectionBeenConfirmed = true) }
    }

    fun onInputFieldChange(key: String, newValue: String) {
        _uiState.value.idInputFieldValues[key] = newValue.trim()
    }

    fun isInputValid(input: String, inputField: InputFieldUi) = inputField.regex.matches(input)

    val currentIdInfo
        get() = IdInfo(
            country = _uiState.value.selectedCountry!!.key,
            idType = _uiState.value.selectedIdType!!.idTypeKey,
            idNumber = _uiState.value.idInputFieldValues["id_number"],
            firstName = _uiState.value.idInputFieldValues["first_name"],
            lastName = _uiState.value.idInputFieldValues["last_name"],
            dob = _uiState.value.idInputFieldValues["dob"],
            bankCode = _uiState.value.idInputFieldValues["bank_code"],
            entered = true,
        )
}

data class InputFieldUi(
    val key: String,
    val label: String,
    val type: Type,
    // "is not blank"
    val regex: Regex = Regex("(.|\\s)*\\S(.|\\s)*"),
) {
    enum class Type {
        Text,
        Date,
        Number,
    }
}

private val inputFieldDetails: Map<String, InputFieldUi> = mapOf(
    "first_name" to InputFieldUi(
        key = "first_name",
        label = "First Name",
        type = InputFieldUi.Type.Text,
    ),
    "last_name" to InputFieldUi(
        key = "last_name",
        label = "Last Name",
        type = InputFieldUi.Type.Text,
    ),
    "id_number" to InputFieldUi(
        key = "id_number",
        label = "ID Number",
        type = InputFieldUi.Type.Text,
    ),
    "dob" to InputFieldUi(
        key = "dob",
        label = "Date of Birth",
        type = InputFieldUi.Type.Date,
    ),
    "day" to InputFieldUi(
        key = "day",
        label = "Day",
        type = InputFieldUi.Type.Number,
    ),
    "month" to InputFieldUi(
        key = "month",
        label = "Month",
        type = InputFieldUi.Type.Number,
    ),
    "year" to InputFieldUi(
        key = "year",
        label = "Year",
        type = InputFieldUi.Type.Number,
    ),
    "citizenship" to InputFieldUi(
        key = "citizenship",
        label = "Citizenship",
        type = InputFieldUi.Type.Text,
    ),
    "bank_code" to InputFieldUi(
        key = "bank_code",
        label = "Bank Code",
        type = InputFieldUi.Type.Number,
    ),
)
