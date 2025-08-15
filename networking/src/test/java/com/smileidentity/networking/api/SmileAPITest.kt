package com.smileidentity.networking.api

import com.smileidentity.networking.di.json
import com.smileidentity.networking.models.AuthenticationRequest
import com.smileidentity.networking.models.JobType
import com.smileidentity.networking.models.ProductsConfigRequest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

    @Test
    fun `should return a valid response when services is called`() = runTest {
        val mockResponse = File("src/test/resources/services.json").readText()

        mockWebServer.enqueue(response = MockResponse().setBody(mockResponse))
        val response = api.getServices()

        assertNotNull(actual = response.bankCodes.size)
        assertContains(charSequence = response.bankCodes[1].name, other = "Citibank")
        assertNotNull(actual = response.hostedWeb.basicKyc.size)
        assertContains(charSequence = response.hostedWeb.basicKyc[0].name, other = "Ghana")
    }

    @Test
    fun `should return valid response when products config is called`() = runTest {
        val mockResponse = File("src/test/resources/products_config.json").readText()

        mockWebServer.enqueue(response = MockResponse().setBody(mockResponse))
        val response =
            api.getProductsConfig(
                request = ProductsConfigRequest(
                    partnerId = "002",
                    signature = "signature",
                ),
            )

        assertNotNull(actual = response.idSelection.basicKyc.size)
        assertNotNull(actual = response.consentRequired.size)
    }

    @Test
    fun `should return valid response when valid documents is called`() = runTest {
        val mockResponse = File("src/test/resources/valid_documents.json").readText()

        mockWebServer.enqueue(response = MockResponse().setBody(mockResponse))
        val response =
            api.getValidDocuments(
                request = ProductsConfigRequest(
                    partnerId = "002",
                    signature = "signature",
                ),
            )

        assertNotNull(actual = response.validDocuments.size)
        assertNotNull(actual = response.validDocuments.first().idTypes.size)
    }
}
