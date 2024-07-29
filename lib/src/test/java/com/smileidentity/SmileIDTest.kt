package com.smileidentity

import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.AuthenticationResponse
import com.smileidentity.models.Config
import com.smileidentity.models.JobType
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.PrepUploadResponse
import com.smileidentity.networking.SmileIDService
import com.smileidentity.util.AUTH_REQUEST_FILE
import com.smileidentity.util.PREP_UPLOAD_REQUEST_FILE
import com.smileidentity.util.cleanupJobs
import com.smileidentity.util.doGetSubmittedJobs
import com.smileidentity.util.doGetUnsubmittedJobs
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.getSmileTempFile
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmileIDTest {
    private val moshi = mockk<Moshi>()
    private val testPath = "some/valid/path"

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockkObject(SmileID)
        every { SmileID.fileSavePath } returns testPath
        mockkStatic("com.smileidentity.util.FileUtilsKt")
        SmileID.config = Config(
            partnerId = "partnerId",
            authToken = "authToken",
            prodBaseUrl = "prodBaseUrl",
            sandboxBaseUrl = "sandboxBaseUrl",
        )
    }

    @Test
    fun `setAllowOfflineMode true sets allowOfflineMode to true`() {
        SmileID.setAllowOfflineMode(true)
        assertEquals(true, SmileID.allowOfflineMode)
    }

    @Test
    fun `setAllowOfflineMode false sets allowOfflineMode to false`() {
        SmileID.setAllowOfflineMode(false)
        assertEquals(false, SmileID.allowOfflineMode)
    }

    @Test
    fun `Should list unsubmitted job ids`() {
        every { doGetUnsubmittedJobs() } returns listOf("job1", "job2")

        SmileID.getUnsubmittedJobs()

        verify { doGetUnsubmittedJobs() }
    }

    @Test
    fun `Should list submitted job ids`() {
        every { doGetSubmittedJobs() } returns listOf("job1", "job2")

        SmileID.getSubmittedJobs()

        verify { doGetSubmittedJobs() }
    }

    @Test
    fun `Should cleanup the correct job id in both submitted and unsubmitted`() {
        // given
        val jobId = "testJobId"
        // when
        SmileID.cleanup(jobId)
        // then
        verify {
            cleanupJobs(
                deleteSubmittedJobs = true,
                deleteUnsubmittedJobs = true,
                jobIds = listOf(jobId),
                savePath = any(),
            )
        }
    }

    @Test
    fun `Should clean a list of jobs`() {
        // given
        val expectedList = listOf("job1", "job2")
        // when
        SmileID.cleanup(jobIds = expectedList)
        // then
        verify {
            cleanupJobs(
                deleteSubmittedJobs = true,
                deleteUnsubmittedJobs = true,
                jobIds = expectedList,
                savePath = any(),
            )
        }
    }

    @Test
    fun `Should clean all jobs`() {
        // when
        SmileID.cleanup()
        // then
        verify {
            cleanupJobs(
                deleteSubmittedJobs = true,
                deleteUnsubmittedJobs = true,
                jobIds = null,
                savePath = any(),
            )
        }
    }

    @Test
    fun `submitJob should throw IllegalArgumentException when jobId is invalid`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            val invalidJobId = "invalidJobId"
            every { doGetUnsubmittedJobs() } returns listOf("jobId1", "jobId2")
            runTest {
                SmileID.submitJob(invalidJobId, true, this).join()
            }
        }
        assertEquals("Invalid jobId or not found", exception.message)
    }

    @Test
    fun `submitJob should fail if there is no AUTH_REQUEST_FILE`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            runTest {
                val jobId = "validJobId"
                every { doGetUnsubmittedJobs() } returns listOf("validJobId")
                every {
                    getSmileTempFile(
                        jobId,
                        AUTH_REQUEST_FILE,
                        any(),
                        any(),
                    )
                } throws IllegalArgumentException("Invalid file name or not found")
                SmileID.submitJob(jobId, true, this).join()
            }
        }
        assertEquals("Invalid file name or not found", exception.message)
    }

    @Test
    fun `submitJob should fail if there is no PRE_UPLOAD_REQUEST_FILE`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            runTest {
                val jobId = "validJobId"
                every { doGetUnsubmittedJobs() } returns listOf("validJobId")
                every {
                    getSmileTempFile(
                        jobId,
                        PREP_UPLOAD_REQUEST_FILE,
                        any(),
                        any(),
                    )
                } throws IllegalArgumentException("Invalid file name or not found")
                SmileID.submitJob(jobId, true, this).join()
            }
        }
        assertEquals("Invalid file name or not found", exception.message)
    }

    @Test
    fun `submitJob completes successfully`() = runTest {
        val api = mockk<SmileIDService>(relaxed = true)
        SmileID.api = api
        val file = mockk<File>(relaxed = true)

        // Mock file operations
        every { getSmileTempFile(any(), any(), any(), any()) } answers {
            val tempFile = File.createTempFile("test", ".json")
            tempFile.writeText("{}")
            tempFile
        }
        every { getFilesByType(any(), any(), any(), any()) } returns listOf(file)
        every { SmileID.moshi } returns moshi // Corrected parameters

        val jobId = "jobId"
        every { doGetUnsubmittedJobs() } returns listOf(jobId)

        // Mock API responses
        val authResponse = mockk<AuthenticationResponse> {
            every { signature } returns "signature"
            every { timestamp } returns "timestamp"
        }
        val prepUploadResponse = mockk<PrepUploadResponse> {
            every { uploadUrl } returns "uploadUrl"
        }
        val prepUploadRequest = mockk<PrepUploadRequest>()
        val authRequestAdapter = mockk<JsonAdapter<AuthenticationRequest>>()
        every { moshi.adapter(AuthenticationRequest::class.java) } returns authRequestAdapter
        val mockAuthRequest: AuthenticationRequest = mockk<AuthenticationRequest>()
        every { mockAuthRequest.authToken = any() } just Runs
        every { mockAuthRequest.jobType } returns JobType.SmartSelfieEnrollment
        every { authRequestAdapter.fromJson(any<String>()) } returns mockAuthRequest

        val prepUploadRequestAdapter = mockk<JsonAdapter<PrepUploadRequest>>()
        every { moshi.adapter(PrepUploadRequest::class.java) } returns prepUploadRequestAdapter
        every { prepUploadRequestAdapter.fromJson(any<String>()) } returns prepUploadRequest

        every {
            prepUploadRequest.copy(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns prepUploadRequest

        coEvery { api.authenticate(any()) } returns authResponse
        coEvery { api.prepUpload(any()) } returns prepUploadResponse
        coEvery { api.upload(any(), any()) } just Runs

        // Execute
        val job = SmileID.submitJob(jobId, true, this)

        // Assert
        job.join() // Waits for the coroutine to complete
        assertTrue(job.isCompleted) // Verify job is completed

        // Verify calls to ensure the API was interacted with as expected
        coVerify { api.authenticate(any()) }
        coVerify { api.prepUpload(any()) }
        coVerify { api.upload(any(), any()) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
