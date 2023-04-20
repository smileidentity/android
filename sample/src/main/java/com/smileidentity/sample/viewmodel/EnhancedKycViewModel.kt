package com.smileidentity.sample.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.ProcessingState
import com.smileidentity.getExceptionHandler
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.EnhancedKycRequest
import com.smileidentity.models.IdType
import com.smileidentity.models.JobType
import com.smileidentity.results.EnhancedKycResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EnhancedKycUiState(
    val selectedCountry: SupportedCountry? = null,
    val selectedIdType: IdType? = null,
    val idInputFieldValues: SnapshotStateMap<IdType.InputField, String> = mutableStateMapOf(),
    val processingState: ProcessingState? = null,
    val errorMessage: String? = null,
)

class EnhancedKycViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EnhancedKycUiState())
    val uiState = _uiState.asStateFlow()
    var result: EnhancedKycResult? = null

    fun doEnhancedKyc() {
        _uiState.update { it.copy(processingState = ProcessingState.InProgress) }
        val proxy = { e: Throwable ->
            result = EnhancedKycResult.Error(e)
            _uiState.update {
                it.copy(processingState = ProcessingState.Error, errorMessage = e.message)
            }
        }
        viewModelScope.launch(getExceptionHandler(proxy)) {
            val authRequest = AuthenticationRequest(
                jobType = JobType.EnhancedKyc,
                enrollment = false,
                userId = UUID.randomUUID().toString(),
            )
            val authResponse = SmileID.api.authenticate(authRequest)
            val enhancedKycRequest = EnhancedKycRequest(
                partnerParams = authResponse.partnerParams,
                signature = authResponse.signature,
                timestamp = authResponse.timestamp,
                country = _uiState.value.selectedIdType!!.countryCode,
                idType = _uiState.value.selectedIdType!!.idType,
                idNumber = _uiState.value.idInputFieldValues[IdType.InputField.IdNumber]!!,
                firstName = _uiState.value.idInputFieldValues[IdType.InputField.FirstName],
                lastName = _uiState.value.idInputFieldValues[IdType.InputField.LastName],
                dob = _uiState.value.idInputFieldValues[IdType.InputField.Dob],
                bankCode = _uiState.value.idInputFieldValues[IdType.InputField.BankCode],
            )
            val response = SmileID.api.doEnhancedKyc(enhancedKycRequest)
            result = EnhancedKycResult.Success(enhancedKycRequest, response)
            _uiState.update { it.copy(processingState = ProcessingState.Success) }
        }
    }

    fun onFinished(callback: EnhancedKycResult.Callback) {
        callback.onResult(result!!)
    }

    fun onCountrySelected(selectedCountry: SupportedCountry) {
        _uiState.update { it.copy(selectedCountry = selectedCountry, selectedIdType = null) }
    }

    fun onIdTypeSelected(selectedIdType: IdType) {
        // map all required fields to empty strings
        _uiState.update {
            it.copy(selectedIdType = selectedIdType, idInputFieldValues = mutableStateMapOf())
        }
    }

    fun onIdInputFieldChanged(it: IdType.InputField, newValue: String) {
        _uiState.value.idInputFieldValues[it] = newValue.trim()
    }

    fun allInputsSatisfied(): Boolean {
        val state = _uiState.value
        return state.selectedCountry != null && state.selectedIdType != null &&
            state.selectedIdType.isValidIdNumber(
                state.idInputFieldValues[IdType.InputField.IdNumber] ?: "",
            ) &&
            state.selectedIdType.requiredFields.all {
                (state.idInputFieldValues[it] ?: "").isNotBlank()
            }
    }

    @StringRes
    fun getFieldDisplayName(field: IdType.InputField) = when (field) {
        IdType.InputField.IdNumber -> R.string.si_id_field_name_id_number
        IdType.InputField.FirstName -> R.string.si_id_field_name_first_name
        IdType.InputField.LastName -> R.string.si_id_field_name_last_name
        IdType.InputField.Dob -> R.string.si_id_field_name_dob
        IdType.InputField.BankCode -> R.string.si_id_field_name_bank_code
    }

    @StringRes
    fun getIdTypeDisplayName(idType: IdType) = when (idType) {
        IdType.GhanaDriversLicense -> R.string.si_id_type_name_drivers_license
        IdType.GhanaPassport -> R.string.si_id_type_name_passport
        IdType.GhanaSSNIT -> R.string.si_id_type_name_ssnit
        IdType.GhanaVoterId -> R.string.si_id_type_name_voter_id
        IdType.GhanaNewVoterId -> R.string.si_id_type_name_new_voter_id
        IdType.KenyaAlienCard -> R.string.si_id_type_name_alien_card
        IdType.KenyaNationalId -> R.string.si_id_type_name_national_id
        IdType.KenyaNationalIdNoPhoto -> R.string.si_id_type_name_national_id_no_photo
        IdType.KenyaPassport -> R.string.si_id_type_name_passport
        IdType.NigeriaBankAccount -> R.string.si_id_type_name_bank_account
        IdType.NigeriaBVN -> R.string.si_id_type_name_bvn
        IdType.NigeriaDriversLicense -> R.string.si_id_type_name_drivers_license
        IdType.NigeriaNINV2 -> R.string.si_id_type_name_nin_v2
        IdType.NigeriaNINSlip -> R.string.si_id_type_name_nin_slip
        IdType.NigeriaVNIN -> R.string.si_id_type_name_v_nin
        IdType.NigeriaPhoneNumber -> R.string.si_id_type_name_phone_number
        IdType.NigeriaVoterId -> R.string.si_id_type_name_voter_id
        IdType.SouthAfricaNationalId -> R.string.si_id_type_name_national_id
        IdType.SouthAfricaNationalIdNoPhoto -> R.string.si_id_type_name_national_id_no_photo
        IdType.UgandaNationalIdNoPhoto -> R.string.si_id_type_name_national_id_no_photo
    }
}

enum class SupportedCountry(
    @StringRes val displayName: Int,
    val flagEmoji: String,
    val supportedIdTypes: List<IdType>,
) {
    Ghana(
        R.string.si_country_name_ghana,
        "🇬🇭",
        listOf(
            IdType.GhanaDriversLicense,
            IdType.GhanaPassport,
            IdType.GhanaSSNIT,
            IdType.GhanaVoterId,
            IdType.GhanaNewVoterId,
        ).filter { it.supportsEnhancedKyc },
    ),
    Kenya(
        R.string.si_country_name_kenya,
        "🇰🇪",
        listOf(
            IdType.KenyaAlienCard,
            IdType.KenyaNationalId,
            IdType.KenyaNationalIdNoPhoto,
            IdType.KenyaPassport,
        ).filter { it.supportsEnhancedKyc },
    ),
    Nigeria(
        R.string.si_country_name_nigeria,
        "🇳🇬",
        listOf(
            IdType.NigeriaBankAccount,
            IdType.NigeriaBVN,
            IdType.NigeriaDriversLicense,
            IdType.NigeriaNINV2,
            IdType.NigeriaNINSlip,
            IdType.NigeriaVNIN,
            IdType.NigeriaPhoneNumber,
            IdType.NigeriaVoterId,
        ).filter { it.supportsEnhancedKyc },
    ),
    SouthAfrica(
        R.string.si_country_name_south_africa,
        "🇿🇦",
        listOf(
            IdType.SouthAfricaNationalId,
            IdType.SouthAfricaNationalIdNoPhoto,
        ).filter { it.supportsEnhancedKyc },
    ),
    Uganda(
        R.string.si_country_name_uganda,
        "🇺🇬",
        listOf(
            IdType.UgandaNationalIdNoPhoto,
        ).filter { it.supportsEnhancedKyc },
    ),
}
