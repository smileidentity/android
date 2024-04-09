package com.smileidentity.networking

import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import timber.log.Timber

annotation class SmileHeaderAuth

/**
 * This interceptor is responsible for authenticating requests on v2 Endpoints. It calls
 * /v1/auth_smile to get the signature and timestamp and adds the necessary headers to the request.
 * This interceptor
 */
object SmileHeaderAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request: Request = chain.request()
        request.getCustomAnnotation(SmileHeaderAuth::class.java) ?: return chain.proceed(request)
        Timber.v("SmileHeaderAuthInterceptor: Interceptor called")
        val authResponse = runBlocking {
            SmileID.api.authenticate(AuthenticationRequest(enrollment = true))
        }
        val newRequest = request.newBuilder()
            .header("SmileID-Partner-ID", SmileID.config.partnerId)
            .header("SmileID-Request-Signature", authResponse.signature)
            .header("SmileID-Timestamp", authResponse.timestamp)
            .build()
        return chain.proceed(newRequest)
    }
}
