package com.smileidentity.networking

import com.smileidentity.networking.models.Config
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("unused")
object SmileIdentity {
    @JvmStatic
    lateinit var api: SmileIdentityService private set
    val moshi: Moshi = initMoshi()
    lateinit var config: Config private set
    private lateinit var retrofit: Retrofit

    // Can't use lateinit on primitives, this default will be overwritten as soon as init is called
    internal var useSandbox: Boolean = true

    internal var apiKey: String? = null

    /**
     * Initialize the SDK. This must be called before any other SDK methods. API calls must first be
     * authenticated with a call to [SmileIdentityService.authenticate], since this initialization
     * method does not use an API Key, but rather the auth token from the Config to create a
     * signature
     *
     * @param config The [Config] to use, from the Smile Identity Portal
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     * @param okHttpClient The [OkHttpClient] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        config: Config,
        useSandbox: Boolean = false,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) {
        this.config = config
        this.useSandbox = useSandbox
        val url = if (useSandbox) config.sandboxBaseUrl else config.prodBaseUrl

        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(UploadRequestConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(SmileIdentityService::class.java)
    }

    /**
     * Initialize the SDK with an API Key. This must be called before any other SDK methods. API
     * keys are different from the auth token in the Config. If this initialization method is used,
     * authToken from [config] need not be used.
     *
     * @param apiKey The API Key to use
     * @param config The [Config] to use
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     * @param okHttpClient The [OkHttpClient] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        apiKey: String,
        config: Config,
        useSandbox: Boolean = false,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) {
        this.apiKey = apiKey
        initialize(config, useSandbox, okHttpClient)
    }

    fun setEnvironment(useSandbox: Boolean) {
        this.useSandbox = useSandbox
        val url = if (useSandbox) config.sandboxBaseUrl else config.prodBaseUrl
        retrofit = retrofit.newBuilder().baseUrl(url).build()
        api = retrofit.create(SmileIdentityService::class.java)
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
        addNetworkInterceptor(
            Interceptor { chain: Interceptor.Chain ->
                // Retry on exception (network error) and 5xx
                val request = chain.request()
                for (attempt in 1..3) {
                    try {
                        // Using Logger here because this networking module has no Android
                        // dependencies, whilst Timber/Log does
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
            },
        )
        addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
    }

    /**
     * Create the [Moshi] instance used by the SDK. It is declared here instead of [initialize]
     * because [Moshi] needs to already be initialized when the UI module attempts to read the
     * config JSON file directly into a [Config], which needs to happen as a prerequisite to the
     * [initialize] call
     */
    private fun initMoshi(): Moshi {
        return Moshi.Builder()
            .add(PartnerParamsAdapter)
            .add(StringifiedBooleanAdapter)
            .add(FileAdapter)
            .add(JobResultAdapter)
            .build()
    }
}
