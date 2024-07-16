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
class SmartSelfieV2ViewModelTest {
    private lateinit var subject: SmartSelfieV2ViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = SmartSelfieV2ViewModel(
            userId = "userId",
            isEnroll = false,
            useStrictMode = false,
            extraPartnerParams = persistentMapOf(),
            selfieQualityModel = mockk(),
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
