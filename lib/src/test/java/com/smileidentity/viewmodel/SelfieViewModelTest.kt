package com.smileidentity.viewmodel

import androidx.camera.core.ImageProxy
import com.smileidentity.R
import com.smileidentity.util.StringResource
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.ujizin.camposer.state.CamSelector
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelfieViewModelTest {
    private lateinit var subject: SelfieViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = SelfieViewModel(
            isEnroll = true,
            userId = randomUserId(),
            jobId = randomJobId(),
            allowNewEnroll = false,
            skipApiSubmission = false,
            metadata = mutableListOf(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        assertEquals(SelfieDirective.InitialInstruction, uiState.directive)
        assertEquals(0f, uiState.progress)
        assertEquals(null, uiState.selfieToConfirm)
        assertEquals(null, uiState.processingState)
        assertEquals(
            StringResource.ResId(R.string.si_processing_error_subtitle),
            uiState.errorMessage,
        )
    }

    @Test
    fun `analyzeImage should close the proxy when capture is already complete`() {
        // given
        val proxy = mockk<ImageProxy>()
        every { proxy.image } returns mockk(relaxed = true)
        every { proxy.close() } returns Unit
        subject.shouldAnalyzeImages = false

        // when
        subject.analyzeImage(proxy, CamSelector.Back)

        // then
        verify(exactly = 1) { proxy.close() }
        verify(exactly = 1) { proxy.image }
        confirmVerified(proxy)
    }
}
