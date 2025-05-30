package com.smileidentity.networking

import android.util.Base64
import com.smileidentity.models.UploadRequest
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class UploadRequestConverterTest {
    private val mockWebServer = MockWebServer()
    private val service = Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .addConverterFactory(UploadRequestConverterFactory)
        .build()
        .create(SmileIDService::class.java)

    @OptIn(ExperimentalEncodingApi::class)
    @Before
    fun setUp() {
        mockkStatic(Base64::class)
        every {
            Base64.encodeToString(any(), any())
        } returns "mocked-base64-string"
    }

    @After
    fun tearDown() {
        unmockkAll()
        mockWebServer.shutdown()
    }

    @Test
    fun `UploadRequest should be sent as zip`() {
        // given
        val response = MockResponse().setResponseCode(200)
        mockWebServer.enqueue(response)
        val uploadRequest = UploadRequest(emptyList())

        // when
        runBlocking { service.upload("", uploadRequest) }

        // then
        val capturedRequest = mockWebServer.takeRequest()
        val contentType = capturedRequest.headers["Content-Type"]
        assertEquals("application/zip", contentType)
    }
}
