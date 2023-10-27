package com.smileidentity.sample.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AvailableIdType
import com.smileidentity.models.CountryInfo
import com.smileidentity.models.IdInfo
import com.smileidentity.models.IdTypes
import com.smileidentity.models.JobType
import com.smileidentity.models.JobType.BiometricKyc
import com.smileidentity.models.JobType.EnhancedDocumentVerification
import com.smileidentity.models.JobType.EnhancedKyc
import com.smileidentity.models.ProductsConfigRequest
import com.smileidentity.models.RequiredField
import com.smileidentity.sample.compose.components.SearchableInputFieldItem
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

data class IdTypeSelectorAndFieldInputUiState(
    val countries: ImmutableList<SearchableInputFieldItem>? = null,
    val selectedCountry: SearchableInputFieldItem? = null,
    val idTypesForCountry: List<AvailableIdType>? = null,
    val selectedIdType: AvailableIdType? = null,
    val hasIdTypeSelectionBeenConfirmed: Boolean = false,
    val idInputFields: List<InputFieldUi>? = null,
    val idInputFieldValues: SnapshotStateMap<RequiredField, String> = mutableStateMapOf(),
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

class IdTypeSelectorAndFieldInputViewModel(
    private val jobType: JobType,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IdTypeSelectorAndFieldInputUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var servicesResponseForJobType: List<CountryInfo>
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
                jobType = jobType,
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val productsConfigRequest = ProductsConfigRequest(
                timestamp = authResponse.timestamp,
                signature = authResponse.signature,
            )
            val productsConfigResponse = SmileID.api.getProductsConfig(productsConfigRequest)
            supportedCountriesAndIdTypes = when (jobType) {
                BiometricKyc -> productsConfigResponse.idSelection.biometricKyc
                EnhancedKyc -> productsConfigResponse.idSelection.enhancedKyc
                EnhancedDocumentVerification -> productsConfigResponse.idSelection.enhancedKyc
                else -> throw IllegalArgumentException("Unsupported JobType: $jobType")
            }

            // Use Services endpoint to get the required input fields for each ID Type as well as
            // the display names
            val servicesResponse = SmileID.api.getServices()
            servicesResponseForJobType = when (jobType) {
                BiometricKyc -> servicesResponse.hostedWeb.biometricKyc
                EnhancedKyc -> servicesResponse.hostedWeb.enhancedKyc
                EnhancedDocumentVerification ->
                    servicesResponse.hostedWeb.enhancedDocumentVerification

                else -> throw IllegalArgumentException("Unsupported JobType: $jobType")
            }
            val countryList = servicesResponseForJobType
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
        val availableIdTypes = servicesResponseForJobType
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
        val ignoredFields = listOf(
            RequiredField.Country,
            RequiredField.IdType,
            RequiredField.UserId,
            RequiredField.JobId,
            RequiredField.Unknown,
        )
        val idInputFields = (idType.requiredFields - ignoredFields).map {
            var inputFieldDetail = getInputFieldUi(it)
            idType.idNumberRegex?.let { regex ->
                if (it == RequiredField.IdNumber) {
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
                idInputFieldValues = mutableStateMapOf<RequiredField, String>().apply {
                    idInputFields.forEach { field -> set(key = field.key, value = "") }
                },
            )
        }
    }

    fun onIdTypeConfirmed() {
        _uiState.update { it.copy(hasIdTypeSelectionBeenConfirmed = true) }
    }

    fun onInputFieldChange(key: RequiredField, newValue: String) {
        _uiState.value.idInputFieldValues[key] = newValue.trim()
    }

    fun isInputValid(input: String, inputField: InputFieldUi) = inputField.regex.matches(input)

    val currentIdInfo
        get() = IdInfo(
            country = _uiState.value.selectedCountry!!.key,
            idType = _uiState.value.selectedIdType!!.idTypeKey,
            idNumber = _uiState.value.idInputFieldValues[RequiredField.IdNumber],
            firstName = _uiState.value.idInputFieldValues[RequiredField.FirstName],
            lastName = _uiState.value.idInputFieldValues[RequiredField.LastName],
            dob = _uiState.value.idInputFieldValues[RequiredField.DateOfBirth],
            bankCode = _uiState.value.idInputFieldValues[RequiredField.BankCode],
            entered = true,
        )

    fun getInputFieldUi(requiredField: RequiredField) = when (requiredField) {
        RequiredField.IdNumber -> InputFieldUi(
            key = requiredField,
            label = "ID Number",
            type = InputFieldUi.Type.Text,
        )

        RequiredField.FirstName -> InputFieldUi(
            key = requiredField,
            label = "First Name",
            type = InputFieldUi.Type.Text,
        )

        RequiredField.LastName -> InputFieldUi(
            key = requiredField,
            label = "Last Name",
            type = InputFieldUi.Type.Text,
        )

        RequiredField.DateOfBirth -> InputFieldUi(
            key = requiredField,
            label = "Date of Birth",
            type = InputFieldUi.Type.Date,
        )

        RequiredField.Day -> InputFieldUi(
            key = requiredField,
            label = "Day",
            type = InputFieldUi.Type.Number,
        )

        RequiredField.Month -> InputFieldUi(
            key = requiredField,
            label = "Month",
            type = InputFieldUi.Type.Number,
        )

        RequiredField.Year -> InputFieldUi(
            key = requiredField,
            label = "Year",
            type = InputFieldUi.Type.Number,
        )

        RequiredField.BankCode -> InputFieldUi(
            key = requiredField,
            label = "Bank Code",
            type = InputFieldUi.Type.Number,
        )

        RequiredField.Citizenship -> InputFieldUi(
            key = requiredField,
            label = "Citizenship",
            type = InputFieldUi.Type.Text,
        )

        else -> throw IllegalArgumentException("Unsupported RequiredField: $requiredField")
    }
}

data class InputFieldUi(
    val key: RequiredField,
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
