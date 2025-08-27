package com.smileidentity.networking

import com.smileidentity.SmileID
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

annotation class SmileIDSdkContextParameter

class SmileIDSdkContextInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        request.getCustomAnnotation(SmileIDSdkContextParameter::class.java)
            ?: return chain.proceed(request)

        val newRequest = request.newBuilder()
            .header("SmileID-loader", SmileID.loader)
            .build()
        return chain.proceed(newRequest)
    }
}
