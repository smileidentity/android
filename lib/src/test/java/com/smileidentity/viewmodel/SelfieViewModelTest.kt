package com.smileidentity.viewmodel

import androidx.camera.core.ImageProxy
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
        assertEquals(null, uiState.errorMessage)
    }

    @Test
    fun `analyzeImage should close the proxy when capture is already complete`() {
        // given
        val proxy = mockk<ImageProxy>()
        every { proxy.image } returns mockk(relaxed = true)
        every { proxy.close() } returns Unit
        subject.shouldAnalyzeImages = false

        // when
        subject.analyzeImage(proxy)

        // then
        verify(exactly = 1) { proxy.close() }
        verify(exactly = 1) { proxy.image }
        confirmVerified(proxy)
    }

    @Test
    fun `submitJob should skip API submission when skipApiSubmission is true`() {
        runTest {
            // Arrange
            val selfieFile = mockk<File>()
            subject.shouldSkipApiSubmission = true
            subject.mockSelfieFile = selfieFile

            // Act
            subject.submitJob()

            // Assert
            val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                subject.uiState.collect()
                assertEquals(ProcessingState.Success, subject.uiState.value.processingState)
            }

            job.cancel()
        }
    }
}
