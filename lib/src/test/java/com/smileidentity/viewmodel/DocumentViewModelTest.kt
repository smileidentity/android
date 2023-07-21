package com.smileidentity.viewmodel

import com.smileidentity.SmileID
import com.smileidentity.compose.components.ProcessingState
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.Config
import com.smileidentity.models.Document
import com.smileidentity.models.JobType
import com.smileidentity.models.PartnerParams
import com.smileidentity.models.PrepUploadResponse
import com.smileidentity.models.UploadRequest
import com.smileidentity.util.randomJobId
import com.smileidentity.util.randomUserId
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewModelTest {
    private lateinit var subject: DocumentViewModel

    private val documentFrontFile = File.createTempFile("documentFront", ".jpg")
    private val selfieFile = File.createTempFile("selfie", ".jpg")
    private val document = Document("KE", "ID_CARD")

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = DocumentViewModel(
            randomUserId(),
            randomJobId(),
            document,
            selfieFile = selfieFile,
        )
        SmileID.config = Config(
            partnerId = "partnerId",
            authToken = "authToken",
            prodBaseUrl = "prodBaseUrl",
            sandboxBaseUrl = "sandboxBaseUrl",
        )
        SmileID.api = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        assertEquals(null, uiState.frontDocumentImageToConfirm)
        assertEquals(null, uiState.errorMessage)
    }

    @Test
    fun `submitJob should move processingState to InProgress`() {
        // when
        subject.submitJob(documentFrontFile)

        // then
        // at this point, the coroutines will not have finished executing
        assertEquals(ProcessingState.InProgress, subject.uiState.value.processingState)
    }

    @Test
    fun `submitJob should move processingState to Error`() = runTest {
        // given
        coEvery { SmileID.api.authenticate(any()) } throws RuntimeException()

        // when
        subject.submitJob(documentFrontFile).join()

        // then
        assertEquals(ProcessingState.Error, subject.uiState.value.processingState)
    }

    @Test
    fun `submitJob should include idInfo`() = runTest {
        // given
        coEvery { SmileID.api.authenticate(any()) } returns AuthenticationResponse(
            success = true,
            signature = "signature",
            timestamp = "timestamp",
            partnerParams = PartnerParams(jobType = JobType.DocumentVerification),
        )

        coEvery { SmileID.api.prepUpload(any()) } returns PrepUploadResponse(
            code = 0,
            refId = "refId",
            uploadUrl = "uploadUrl",
            smileJobId = "smileJobId",
            cameraConfig = null,
        )

        val uploadBodySlot = slot<UploadRequest>()
        coEvery { SmileID.api.upload(any(), capture(uploadBodySlot)) } just Runs

        // when
        subject.submitJob(documentFrontFile).join()

        // then
        assertNotNull(uploadBodySlot.captured.idInfo)
        assertEquals(document.countryCode, uploadBodySlot.captured.idInfo?.country)
        assertEquals(document.documentType, uploadBodySlot.captured.idInfo?.idType)
    }
}
