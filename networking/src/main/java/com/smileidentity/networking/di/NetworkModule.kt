package com.smileidentity.networking.di

import com.smileidentity.networking.api.SmileAPI
import com.smileidentity.networking.interceptor.GzipRequestInterceptor
import com.smileidentity.networking.interceptor.SmileHeaderAuthInterceptor
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Create the SmileService instance used by the SDK.
 */
internal fun initSmileAPI(
    useSandbox: Boolean,
    okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
): SmileAPI {
    val url =
        if (useSandbox) {
            "https://devapi.smileidentity.com/"
        } else {
            "https://api.smileidentity.com/"
        }

    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(
            json.asConverterFactory(
                "application/json; charset=UTF8".toMediaType(),
            ),
        )
        .build()

    return retrofit.create(SmileAPI::class.java)
}

/**
 * Returns an [OkHttpClient.Builder] optimized for low bandwidth conditions. Use it as a
 * starting point if you need to customize an [OkHttpClient] for your own needs
 */
internal fun getOkHttpClientBuilder() = OkHttpClient.Builder().apply {
    callTimeout(timeout = 120, TimeUnit.SECONDS)
    connectTimeout(timeout = 60, unit = TimeUnit.SECONDS)
    readTimeout(timeout = 60, unit = TimeUnit.SECONDS)
    writeTimeout(timeout = 60, unit = TimeUnit.SECONDS)
    addInterceptor(interceptor = SmileHeaderAuthInterceptor)
    // NB! This is the last interceptor so that the logging interceptors come before the request
    //  is gzipped
    // We gzip all requests by default. While supported for Smile ID, it may not be supported by
    //  all servers
    addInterceptor(GzipRequestInterceptor())
}
