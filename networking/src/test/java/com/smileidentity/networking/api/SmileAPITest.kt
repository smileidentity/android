package com.smileidentity.networking.api

import com.smileidentity.networking.di.json
import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.JobType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class SmileAPITest {
    private lateinit var api: SmileAPI
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply(block = MockWebServer::start)
        api =
            Retrofit.Builder()
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .baseUrl(mockWebServer.url("/"))
                .build()
                .create(SmileAPI::class.java)
    }

    @Test
    fun `should return success and timestamp when auth is successful`() = runTest {
        val mockResponse = File("src/test/resources/auth_response.json").readText()

        mockWebServer.enqueue(response = MockResponse().setBody(mockResponse))
        val response = api.authenticate(
            request = AuthenticationRequest(
                production = true,
                partnerId = "002",
                authToken = "random-token",
            ),
        )

        assertEquals(expected = response.success, actual = true)
        assertEquals(expected = response.timestamp, actual = "timestamp")
    }

    @Test
    fun `should return partner params when auth is successful`() = runTest {
        val mockResponse = File("src/test/resources/auth_response.json").readText()

        mockWebServer.enqueue(response = MockResponse().setBody(mockResponse))
        val response = api.authenticate(
            request = AuthenticationRequest(
                production = true,
                partnerId = "002",
                authToken = "random-token",
            ),
        )

        assertEquals(
            expected = response.partnerParams.jobType,
            actual = JobType.DocumentVerification,
        )
        assertEquals(expected = response.partnerParams.jobId, actual = "job_id")
        assertContains(charSequence = response.partnerParams.userId, other = "user-")
    }
}
