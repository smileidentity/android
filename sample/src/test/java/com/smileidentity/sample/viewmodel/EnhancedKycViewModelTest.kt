package com.smileidentity.sample.viewmodel

import com.smileidentity.networking.models.IdType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EnhancedKycViewModelTest {
    private lateinit var subject: EnhancedKycViewModel
    private val uiState get() = subject.uiState.value

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = EnhancedKycViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should initialize with the correct defaults`() {
        assertNull(uiState.selectedCountry)
        assertNull(uiState.selectedIdType)
        assertTrue(uiState.idInputFieldValues.isEmpty())
        assertFalse(uiState.isWaitingForResult)
    }

    @Test
    fun `uiState should update when country is selected`() {
        // when
        subject.onCountrySelected(SupportedCountry.Uganda)

        // then
        assertTrue(uiState.selectedCountry == SupportedCountry.Uganda)
        assertNull(uiState.selectedIdType)
    }

    @Test
    fun `uiState should update when ID type is selected`() {
        // when
        subject.onIdTypeSelected(IdType.GhanaPassport)

        // then
        assertTrue(uiState.selectedIdType == IdType.GhanaPassport)
        assertTrue(uiState.idInputFieldValues.isEmpty())
    }

    @Test
    fun `uiState should update when input field changes`() {
        // given
        subject.onIdTypeSelected(IdType.NigeriaDriversLicense)

        // when
        subject.onIdInputFieldChanged(IdType.InputField.IdNumber, "1234567890")
        subject.onIdInputFieldChanged(IdType.InputField.FirstName, "John")
        subject.onIdInputFieldChanged(IdType.InputField.LastName, "Doe")
        subject.onIdInputFieldChanged(IdType.InputField.Dob, "01/01/2000")
        subject.onIdInputFieldChanged(IdType.InputField.BankCode, "1234567890")

        // then
        assertTrue(uiState.idInputFieldValues[IdType.InputField.IdNumber] == "1234567890")
        assertTrue(uiState.idInputFieldValues[IdType.InputField.FirstName] == "John")
        assertTrue(uiState.idInputFieldValues[IdType.InputField.LastName] == "Doe")
        assertTrue(uiState.idInputFieldValues[IdType.InputField.Dob] == "01/01/2000")
        assertTrue(uiState.idInputFieldValues[IdType.InputField.BankCode] == "1234567890")
    }

    @Test
    fun `allInputsSatisfied should be true when all inputs satisfied`() {
        // given
        subject.onCountrySelected(SupportedCountry.Nigeria)
        subject.onIdTypeSelected(IdType.NigeriaDriversLicense)
        subject.onIdInputFieldChanged(IdType.InputField.IdNumber, "AAA-123456")
        subject.onIdInputFieldChanged(IdType.InputField.FirstName, "John")
        subject.onIdInputFieldChanged(IdType.InputField.LastName, "Doe")
        subject.onIdInputFieldChanged(IdType.InputField.Dob, "01/01/2000")

        // when
        val result = subject.allInputsSatisfied()

        // then
        assertTrue(result)
    }

    @Test
    fun `allInputsSatisfied should be false when ID number format invalid`() {
        // given
        subject.onCountrySelected(SupportedCountry.Nigeria)
        subject.onIdTypeSelected(IdType.NigeriaDriversLicense)
        subject.onIdInputFieldChanged(IdType.InputField.IdNumber, "AAA")
        subject.onIdInputFieldChanged(IdType.InputField.FirstName, "John")
        subject.onIdInputFieldChanged(IdType.InputField.LastName, "Doe")
        subject.onIdInputFieldChanged(IdType.InputField.Dob, "01/01/2000")

        // when
        val result = subject.allInputsSatisfied()

        // then
        assertFalse(result)
    }

    @Test
    fun `allInputsSatisfied should be false when missing all inputs`() {
        // given
        subject.onCountrySelected(SupportedCountry.Nigeria)
        subject.onIdTypeSelected(IdType.NigeriaDriversLicense)

        // when
        val result = subject.allInputsSatisfied()

        // then
        assertFalse(result)
    }

    @Test
    fun `allInputsSatisfied should be false when missing some inputs`() {
        // given
        subject.onCountrySelected(SupportedCountry.Nigeria)
        subject.onIdTypeSelected(IdType.NigeriaDriversLicense)
        subject.onIdInputFieldChanged(IdType.InputField.IdNumber, "1234567890")
        subject.onIdInputFieldChanged(IdType.InputField.FirstName, "John")
        subject.onIdInputFieldChanged(IdType.InputField.LastName, "Doe")

        // when
        val result = subject.allInputsSatisfied()

        // then
        assertFalse(result)
    }

    @Test
    fun `allInputsSatisfied should be false when empty strings`() {
        // given
        subject.onCountrySelected(SupportedCountry.Nigeria)
        subject.onIdTypeSelected(IdType.NigeriaDriversLicense)
        subject.onIdInputFieldChanged(IdType.InputField.IdNumber, "1234567890")
        subject.onIdInputFieldChanged(IdType.InputField.FirstName, "John")
        subject.onIdInputFieldChanged(IdType.InputField.LastName, "Doe")
        subject.onIdInputFieldChanged(IdType.InputField.Dob, "")

        // when
        val result = subject.allInputsSatisfied()

        // then
        assertFalse(result)
    }

    @Test
    fun `should do Enhanced KYC`() {
        // given
        subject.onCountrySelected(SupportedCountry.Ghana)
        subject.onIdTypeSelected(IdType.GhanaPassport)
        subject.onIdInputFieldChanged(IdType.InputField.IdNumber, "1234567890")
        var callbackInvoked = false

        // when
        subject.doEnhancedKyc { callbackInvoked = true }

        // then
        assertTrue(uiState.isWaitingForResult)
        assertTrue(callbackInvoked)
    }
}
