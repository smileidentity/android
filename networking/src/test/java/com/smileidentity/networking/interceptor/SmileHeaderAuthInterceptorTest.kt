package com.smileidentity.networking.interceptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.Before

class SmileHeaderAuthInterceptorTest {
    private val subject = SmileHeaderAuthInterceptor
    private lateinit var request: Request

    @Before
    fun setUp() {
        request =
            Request.Builder()
                .url("http://api.smileidentity.com")
                .post(mockk())
                .build()
    }

    @Test
    fun `ensure that the header interceptor is applied`() {
        val chain = mockk<Interceptor.Chain>()
        val capturedRequest = slot<Request>()

        every { chain.request() } returns request
        every { chain.proceed(capture(capturedRequest)) } returns mockk()

        subject.intercept(chain)

        verify { chain.proceed(any()) }
        assertNotNull(capturedRequest.captured.body)
        assertEquals(request.method, capturedRequest.captured.method)
        assertEquals(request.url, capturedRequest.captured.url)
    }
}
