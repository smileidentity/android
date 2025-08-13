package com.smileidentity.networking

import android.content.Context
import com.smileidentity.attestation.SmileIDIntegrityManager
import com.smileidentity.attestation.SmileIDStandardRequestIntegrityManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class SmileIDIntegrityInterceptor(context: Context) : Interceptor {
    private val smileIDIntegrityManager: SmileIDIntegrityManager =
        SmileIDStandardRequestIntegrityManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token =
            try {
                runBlocking { smileIDIntegrityManager.requestToken().getOrThrow() }
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        val chainRequest = chain.request()
                    .newBuilder()
                    .addHeader("SmileID-token", token.toString())
                    .build()
        return chain.proceed(chainRequest)
    }
}
