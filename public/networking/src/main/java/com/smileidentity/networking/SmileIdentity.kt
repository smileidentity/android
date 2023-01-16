package com.smileidentity.networking

import com.smileidentity.networking.models.Config
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("unused")
object SmileIdentity {
    lateinit var config: Config
    lateinit var moshi: Moshi

    /**
     * Initialize the SDK. This must be called before any other SDK methods.
     *
     * @param config The [Config] to use
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     * @param okHttpClientBuilder An optional [OkHttpClient.Builder] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        config: Config,
        useSandbox: Boolean = false,
        okHttpClientBuilder: OkHttpClient = getOkHttpClientBuilder().build(),
    ): SmileIdentityService {
        this.config = config
        this.moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(StringifiedBooleanAdapter)
            .add(FileAdapter)
            .add(JobResultAdapter)
            .build()

        val url = if (useSandbox) config.sandboxBaseUrl else config.baseUrl

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClientBuilder)
            .addConverterFactory(UploadRequestConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SmileIdentityService::class.java)
    }

    /**
     * Returns an [OkHttpClient.Builder] optimized for low bandwidth conditions. Use it as a
     * starting point if you need to customize an [OkHttpClient] for your own needs
     */
    @JvmStatic
    fun getOkHttpClientBuilder() = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(true)
        callTimeout(30, TimeUnit.SECONDS)
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
        addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
            // Retry on exception (network error) and 5xx
            val request = chain.request()
            for (attempt in 1..3) {
                try {
                    // Using Logger here/Can't user Timber because this module has no Android
                    // dependencies, but Timber/Log does
                    Logger.getLogger(SmileIdentity::class.simpleName)
                        .log(Level.FINE, "Smile Identity SDK network attempt #$attempt")
                    // println("Smile Identity network request attempt #$attempt")
                    val response = chain.proceed(request)
                    if (response.code < 500) {
                        return@Interceptor response
                    }
                } catch (e: Exception) {
                    // Network failures end up here. Retry these
                }
            }
            return@Interceptor chain.proceed(request)
        })
        addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
    }
}

