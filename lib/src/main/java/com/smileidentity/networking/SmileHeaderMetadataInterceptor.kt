package com.smileidentity.networking

import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.util.getCurrentIsoTimestamp
import okhttp3.Interceptor

annotation class SmileHeaderMetadata

object SmileHeaderMetadataInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        request.getCustomAnnotation(SmileHeaderMetadata::class.java)
            ?: return chain.proceed(request)
        val newRequest = request.newBuilder()
            .header("Connection", "Keep-Alive")
            .header("SmileID-loader", SmileID.loader)
            .header("SmileID-Source-SDK", "android")
            .header("SmileID-Source-SDK-Version", BuildConfig.VERSION_NAME)
            .header("SmileID-Request-Timestamp", getCurrentIsoTimestamp())
            .build()
        return chain.proceed(newRequest)
    }
}
