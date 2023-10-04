package com.smileidentity.viewmodel.document

import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.Config
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import com.smileidentity.viewmodel.OrchestratedEnhancedDocVViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class OrchestratedEnhancedDocVViewModelTest {
    private lateinit var subject: OrchestratedEnhancedDocVViewModel

    private val documentFrontFile = File.createTempFile("documentFront", ".jpg")

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = OrchestratedEnhancedDocVViewModel(
            randomUserId(),
            randomJobId(),
            countryCode = "KE",
            documentType = "ID_CARD",
            captureBothSides = false,
        )
        SmileID.config = Config(
            partnerId = "partnerId",
            authToken = "authToken",
            prodBaseUrl = "prodBaseUrl",
            sandboxBaseUrl = "sandboxBaseUrl",
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        Assert.assertEquals(DocumentCaptureFlow.SelfieCapture, uiState.currentStep)
        Assert.assertEquals(null, uiState.errorMessage)
    }

    @Test
    fun `processingState should move to InProgress `() {
        // given
        SmileID.api = mockk(relaxed = true)
        coEvery { SmileID.api.authenticate(any()) } coAnswers {
            delay(1000)
            throw RuntimeException("unreachable")
        }

        // when
        subject.onDocumentFrontCaptureSuccess(documentFrontFile)

        // then
        // the submitJob coroutine won't have finished executing yet, so should still be processing
        val currentStep = subject.uiState.value.currentStep
        MatcherAssert.assertThat(
            currentStep,
            CoreMatchers.instanceOf(DocumentCaptureFlow.ProcessingScreen::class.java),
        )
        Assert.assertEquals(
            ProcessingState.InProgress,
            (currentStep as DocumentCaptureFlow.ProcessingScreen).processingState,
        )
    }

    @Test
    fun `submitJob should move processingState to Error`() = runTest {
        // given
        SmileID.api = mockk()
        coEvery { SmileID.api.authenticate(any()) } throws RuntimeException()

        // when
        subject.onDocumentFrontCaptureSuccess(documentFrontFile)

        // then
        val currentStep = subject.uiState.value.currentStep
        val processingScreen = currentStep as? DocumentCaptureFlow.ProcessingScreen
        Assert.assertEquals(ProcessingState.Error, processingScreen?.processingState)
    }
}
