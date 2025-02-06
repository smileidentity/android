package com.smileidentity.viewmodel

import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmartSelfieEnhancedViewModelTest {
    private lateinit var subject: SmartSelfieEnhancedViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = SmartSelfieEnhancedViewModel(
            userId = "userId",
            isEnroll = false,
            extraPartnerParams = persistentMapOf(),
            selfieQualityModel = mockk(),
            skipApiSubmission = true,
            faceDetector = mockk(),
            metadata = mutableListOf(),
            onResult = {},
        )
    }

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        assertEquals(SelfieState.Analyzing(SelfieHint.SearchingForFace), uiState.selfieState)
    }
}
