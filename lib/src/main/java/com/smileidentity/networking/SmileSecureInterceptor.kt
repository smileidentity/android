package com.smileidentity.networking

import okhttp3.Interceptor
import okio.Buffer
import timber.log.Timber

annotation class SmileSecure

object SmileSecureInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        request.getCustomAnnotation(SmileSecure::class.java)
            ?: return chain.proceed(request)

        Timber.d("Smile Secure URL ${request.url}")
        request.headers.forEach { (name, value) ->
            Timber.d("Smile Secure Headers $name and $value")
        }
        request.body?.let { requestBody ->
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val bodyStr = buffer.readUtf8()
            Timber.d("Smile Secure Body $bodyStr")
        }

        val modifiedRequest = request.newBuilder()
            .header("SmileID-Request-Timestamp", "")
            .header("SmileID-Request-Hash", "")
            .build()

        return chain.proceed(modifiedRequest)
    }
}
