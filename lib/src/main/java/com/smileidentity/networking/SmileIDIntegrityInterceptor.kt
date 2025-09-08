package com.smileidentity.networking

import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import io.sentry.SentryLevel
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

annotation class SmileIDIntegrityHeader

class SmileIDIntegrityInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var token: String? = null
        request.getCustomAnnotation(SmileIDIntegrityHeader::class.java)
            ?: return chain.proceed(request)
        val macHeader = request.header("SmileID-Request-Mac") ?: return chain.proceed(request)
        Timber.v("SmileIDIntegrityInterceptor: Interceptor called")
        runBlocking {
            try {
                SmileID.integrityManager.requestToken(
                    requestHash = macHeader,
                ).onSuccess {
                    token = it
                }.onFailure {
                    SmileIDCrashReporting.scopes.captureMessage(
                        "SmileIDIntegrity Token Request Failed",
                    ) { scope ->
                        scope.level = SentryLevel.ERROR
                        scope.setExtra("macHeader", macHeader)
                        scope.setExtra("error", it.toString())
                    }
                }
            } catch (e: Exception) {
                // https://stackoverflow.com/a/58711127/3831060
                // OkHttp only propagates IOExceptions, so we need to catch HttpException (which can
                // occur when credentials are not valid, for example) and rethrow it, so that the
                // exception handlers at the application level can handle it. We add the caught
                // exception as a suppressed exception to the IOException that we throw, so that
                // [getExceptionHandler] can still access the original exception.
                throw IOException("Error adding token").apply {
                    addSuppressed(e)
                }
            }
        }
        token ?: return chain.proceed(request)
        val chainRequest = request.newBuilder()
            .header("SmileID-Device-Nonce", token)
            .build()
        return chain.proceed(chainRequest)
    }
}
