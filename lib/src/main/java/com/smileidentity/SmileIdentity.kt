package com.smileidentity

import android.content.Context
import com.serjltt.moshi.adapters.FallbackEnum
import com.smileidentity.models.Config
import com.smileidentity.networking.FileAdapter
import com.smileidentity.networking.JobResultAdapter
import com.smileidentity.networking.JobTypeAdapter
import com.smileidentity.networking.PartnerParamsAdapter
import com.smileidentity.networking.SmileIdentityService
import com.smileidentity.networking.StringifiedBooleanAdapter
import com.smileidentity.networking.UploadRequestConverterFactory
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Suppress("unused")
object SmileIdentity {
    @JvmStatic
    lateinit var api: SmileIdentityService private set
    val moshi: Moshi = initMoshi() // Initialized immediately so it can be used to parse Config
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
     * @param enableCrashReporting Whether to enable crash reporting for *ONLY* Smile
     * Identity related crashes. This is powered by Sentry, and further details on inner workings
     * can be found in the source docs for [SmileIdentityCrashReporting]
     * @param okHttpClient The [OkHttpClient] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        config: Config,
        useSandbox: Boolean = false,
        enableCrashReporting: Boolean = false,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) {
        SmileIdentity.config = config
        // Enable crash reporting as early as possible (the pre-req is that the config is loaded)
        if (enableCrashReporting) {
            SmileIdentityCrashReporting.enable()
        }
        SmileIdentity.useSandbox = useSandbox
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
     * @param enableCrashReporting Whether to enable crash reporting for *ONLY* Smile
     * Identity related crashes. This is powered by Sentry, and further details on inner workings
     * can be found in the source docs for [SmileIdentityCrashReporting]
     * @param okHttpClient The [OkHttpClient] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        apiKey: String,
        config: Config,
        useSandbox: Boolean = false,
        enableCrashReporting: Boolean = false,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) {
        SmileIdentity.apiKey = apiKey
        initialize(config, useSandbox, enableCrashReporting, okHttpClient)
    }

    /**
     * Initialize the SDK. This must be called before any other SDK methods.
     *
     * @param context A [Context] instance which will be used to load the config file from assets
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     * @param enableCrashReporting Whether to enable crash reporting for *ONLY* Smile
     * Identity related crashes. This is powered by Sentry, and further details on inner workings
     * can be found in the source docs for [SmileIdentityCrashReporting]
     * @param okHttpClient An optional [OkHttpClient.Builder] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        context: Context,
        useSandbox: Boolean = false,
        enableCrashReporting: Boolean = false,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) = initialize(Config.fromAssets(context), useSandbox, enableCrashReporting, okHttpClient)

    /**
     * Initialize the SDK with an API Key. This must be called before any other SDK methods. API
     * keys are different from the auth token in the Config. If this initialization method is used,
     * authToken from [config] need not be used.
     *
     * @param apiKey The API Key to use
     * @param context A [Context] instance which will be used to load the config file from assets
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     * @param enableCrashReporting Whether to enable crash reporting for *ONLY* Smile
     * Identity related crashes. This is powered by Sentry, and further details on inner workings
     * can be found in the source docs for [SmileIdentityCrashReporting]
     * @param okHttpClient The [OkHttpClient] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        apiKey: String,
        context: Context,
        useSandbox: Boolean = false,
        enableCrashReporting: Boolean = false,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) = initialize(
        apiKey,
        Config.fromAssets(context),
        useSandbox,
        enableCrashReporting,
        okHttpClient,
    )

    fun setEnvironment(useSandbox: Boolean) {
        SmileIdentity.useSandbox = useSandbox
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
                        Timber.v("Smile Identity SDK network attempt #$attempt")
                        val response = chain.proceed(request)
                        if (response.code < 500) {
                            return@Interceptor response
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Smile Identity SDK network attempt #$attempt failed")
                        // Network failures end up here. These will be retried
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
            .add(JobTypeAdapter)
            .add(PartnerParamsAdapter)
            .add(StringifiedBooleanAdapter)
            .add(FileAdapter)
            .add(JobResultAdapter)
            .add(FallbackEnum.ADAPTER_FACTORY)
            .build()
    }
}
