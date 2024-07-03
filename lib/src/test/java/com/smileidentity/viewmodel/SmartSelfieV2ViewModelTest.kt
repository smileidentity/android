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
            extraPartnerParams = persistentMapOf(),
            selfieQualityModel = mockk(),
            faceDetector = mockk(),
            onResult = {},
        )
    }

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        assertEquals(SelfieHint.SearchingForFace, uiState.selfieHint)
        assertEquals(false, uiState.showLoading)
        assertEquals(false, uiState.showCompletion)
        assertEquals(false, uiState.showBorderHighlight)
    }
}
