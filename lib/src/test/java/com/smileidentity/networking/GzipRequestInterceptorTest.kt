package com.smileidentity.networking

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

internal class GzipRequestInterceptorTest {
    private val subject = GzipRequestInterceptor()

    @Test
    fun `skip if encoding already set`() {
        // given
        val request = Request.Builder()
            .url("http://www.smileidentity.com")
            .header("Content-Encoding", "identity")
            .build()
        val chain = mockk<Interceptor.Chain>()
        val capturedRequest = slot<Request>()
        every { chain.request() } returns request
        every { chain.proceed(capture(capturedRequest)) } returns mockk()

        // when
        subject.intercept(chain)

        // then
        verify { chain.proceed(any()) }
        assert(capturedRequest.captured.header("Content-Encoding") == "identity")
    }

    @Test
    fun `skip if body is null`() {
        // given
        val request = Request.Builder()
            .url("http://www.smileidentity.com")
            .build()
        val chain = mockk<Interceptor.Chain>()
        val capturedRequest = slot<Request>()
        every { chain.request() } returns request
        every { chain.proceed(capture(capturedRequest)) } returns mockk()

        // when
        subject.intercept(chain)

        // then
        verify { chain.proceed(any()) }
        assert(capturedRequest.captured.body == null)
        assert(capturedRequest.captured.header("Content-Encoding") == null)
    }

    @Test
    fun `apply gzip encoding`() {
        // given
        val request = Request.Builder()
            .url("http://www.smileidentity.com")
            .post(mockk())
            .build()
        val chain = mockk<Interceptor.Chain>()
        val capturedRequest = slot<Request>()
        every { chain.request() } returns request
        every { chain.proceed(capture(capturedRequest)) } returns mockk()

        // when
        subject.intercept(chain)

        // then
        verify { chain.proceed(any()) }
        assertNotNull(capturedRequest.captured.body)
        assertEquals("gzip", capturedRequest.captured.header("Content-Encoding"))
        assertEquals(-1L, capturedRequest.captured.body?.contentLength())
        assertEquals(request.method, capturedRequest.captured.method)
        assertEquals(request.url, capturedRequest.captured.url)
    }
}
