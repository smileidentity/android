package com.smileidentity.viewmodel.document

import com.smileidentity.R
import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.Config
import com.smileidentity.models.DocumentCaptureFlow
import com.smileidentity.models.DocumentVerificationJobStatusResponse
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadResponse
import com.smileidentity.models.UploadRequest
import com.smileidentity.results.DocumentVerificationResult
import com.smileidentity.results.SmartSelfieResult
import com.smileidentity.results.SmileIDResult
import com.smileidentity.util.StringResource
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewModelTest {
    private lateinit var subject: OrchestratedDocumentViewModel<DocumentVerificationResult>

    private val documentFrontFile = File.createTempFile("documentFront", ".jpg")
    private val selfieFile = File.createTempFile("selfie", ".jpg")

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = DocumentVerificationViewModel(
            jobType = JobType.DocumentVerification,
            userId = randomUserId(),
            jobId = randomJobId(),
            allowNewEnroll = false,
            countryCode = "KE",
            documentType = "ID_CARD",
            selfieFile = selfieFile,
            captureBothSides = false,
            metadata = mutableListOf(),
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
        assertEquals(DocumentCaptureFlow.FrontDocumentCapture, uiState.currentStep)
        assertEquals(
            StringResource.ResId(R.string.si_processing_error_subtitle),
            uiState.errorMessage,
        )
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
        assertThat(currentStep, instanceOf(DocumentCaptureFlow.ProcessingScreen::class.java))
        assertEquals(
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
        assertEquals(ProcessingState.Error, processingScreen?.processingState)
    }

    @Test
    fun `submitJob should include idInfo`() = runTest {
        // given
        SmileID.api = mockk()
        coEvery { SmileID.api.authenticate(any()) } returns AuthenticationResponse(
            success = true,
            signature = "signature",
            timestamp = "timestamp",
            partnerParams = PartnerParams(jobType = JobType.DocumentVerification),
        )

        coEvery { SmileID.api.prepUpload(any()) } returns PrepUploadResponse(
            code = "0",
            refId = "refId",
            uploadUrl = "uploadUrl",
            smileJobId = "smileJobId",
        )

        coEvery { SmileID.api.getDocumentVerificationJobStatus(any()) } returns
            DocumentVerificationJobStatusResponse(
                timestamp = "timestamp",
                jobComplete = true,
                jobSuccess = true,
                code = "0",
                history = null,
                imageLinks = null,
                result = null,
            )

        val uploadBodySlot = slot<UploadRequest>()
        coEvery { SmileID.api.upload(any(), capture(uploadBodySlot)) } just Runs

        // when
        subject.onDocumentFrontCaptureSuccess(documentFrontFile)

        // then
        assertNotNull(uploadBodySlot.captured.idInfo)
        assertEquals("KE", uploadBodySlot.captured.idInfo?.country)
        assertEquals("ID_CARD", uploadBodySlot.captured.idInfo?.idType)
    }

    @Test
    fun `should submit liveness photos after selfie capture`() = runTest {
        SmileID.api = mockk()
        val selfieResult = SmartSelfieResult(
            selfieFile = selfieFile,
            livenessFiles = listOf(File.createTempFile("liveness", ".jpg")),
            apiResponse = null,
        )
        coEvery { SmileID.api.authenticate(any()) } returns AuthenticationResponse(
            success = true,
            signature = "signature",
            timestamp = "timestamp",
            partnerParams = PartnerParams(jobType = JobType.DocumentVerification),
        )

        coEvery { SmileID.api.prepUpload(any()) } returns PrepUploadResponse(
            code = "0",
            refId = "refId",
            uploadUrl = "uploadUrl",
            smileJobId = "smileJobId",
        )

        coEvery { SmileID.api.getDocumentVerificationJobStatus(any()) } returns
            DocumentVerificationJobStatusResponse(
                timestamp = "timestamp",
                jobComplete = true,
                jobSuccess = true,
                code = "0",
                history = null,
                imageLinks = null,
                result = null,
            )

        val uploadBodySlot = slot<UploadRequest>()
        coEvery { SmileID.api.upload(any(), capture(uploadBodySlot)) } just Runs

        // when
        subject.onDocumentFrontCaptureSuccess(documentFrontFile)
        subject.onSelfieCaptureSuccess(SmileIDResult.Success(selfieResult))

        // then
        // 3 <- selfie file + document front file + 1 liveness file
        assertEquals(3, uploadBodySlot.captured.images.size)
    }
}
