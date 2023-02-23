package com.smileidentity.networking

import com.smileidentity.networking.models.UploadRequest
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit

class UploadRequestConverterTest {
    private val mockWebServer = MockWebServer()
    private val service = Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .addConverterFactory(UploadRequestConverterFactory)
        .build()
        .create(SmileIdentityService::class.java)

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
