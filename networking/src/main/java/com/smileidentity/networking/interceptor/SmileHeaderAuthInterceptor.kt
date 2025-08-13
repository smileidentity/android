package com.smileidentity.networking.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation

annotation class SmileHeaderAuth

/**
 * This interceptor is responsible for authenticating requests on v2 Endpoints. It calls
 * /v1/auth_smile to get the signature and timestamp and adds the necessary headers to the request.
 */
object SmileHeaderAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        request.getCustomAnnotation(SmileHeaderAuth::class.java) ?: return chain.proceed(request)
        val newRequest =
            request.newBuilder()
                .header(name = "SmileID-Partner-ID", value = "partnerId")
                .header(name = "SmileID-Request-Signature", value = "signature")
                .header(name = "SmileID-Timestamp", value = "timestamp")
                .build()
        return chain.proceed(newRequest)
    }
}

fun <T : Annotation> Request.getCustomAnnotation(annotationClass: Class<T>): T? =
    this.tag(type = Invocation::class.java)?.method()?.getAnnotation(annotationClass)
