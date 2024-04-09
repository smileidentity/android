package com.smileidentity.networking

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.GzipSink
import okio.buffer
import okio.use
import timber.log.Timber

/**
 * This interceptor compresses the HTTP request body. It is included by default for all SmileID
 * requests. If this is used for other websites, it is possible they may not support gzip encoding.
 */
internal class GzipRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val body = originalRequest.body
        val isValidHost = originalRequest.url.host.contains("api.smileidentity.com")
        if (body == null || originalRequest.header("Content-Encoding") != null || !isValidHost) {
            return chain.proceed(originalRequest)
        }

        val compressedRequest: Request = originalRequest.newBuilder()
            .header("Content-Encoding", "gzip")
            .method(originalRequest.method, gzip(body))
            .build()
        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType() = body.contentType()

            // We don't know the compressed length in advance
            override fun contentLength(): Long = -1

            override fun writeTo(sink: BufferedSink) = GzipSink(sink).buffer().use {
                Timber.v("Gzipping request body")
                body.writeTo(it)
            }
        }
    }
}
