package com.smileidentity

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.face.FaceDetection
import com.serjltt.moshi.adapters.FallbackEnum
import com.smileidentity.models.Config
import com.smileidentity.networking.BiometricKycJobResultAdapter
import com.smileidentity.networking.DocumentVerificationJobResultAdapter
import com.smileidentity.networking.EnhancedDocumentVerificationJobResultAdapter
import com.smileidentity.networking.FileAdapter
import com.smileidentity.networking.JobResultAdapter
import com.smileidentity.networking.JobTypeAdapter
import com.smileidentity.networking.PartnerParamsAdapter
import com.smileidentity.networking.SmartSelfieJobResultAdapter
import com.smileidentity.networking.SmileIDService
import com.smileidentity.networking.StringifiedBooleanAdapter
import com.smileidentity.networking.UploadRequestConverterFactory
import com.smileidentity.util.cleanupJobs
import com.smileidentity.util.listJobIds
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.net.URL
import java.util.concurrent.TimeUnit

@Suppress("unused")
object SmileID {
    @JvmStatic
    lateinit var api: SmileIDService internal set
    val moshi: Moshi = initMoshi() // Initialized immediately so it can be used to parse Config

    lateinit var config: Config
        internal set
    private lateinit var retrofit: Retrofit

    // Can't use lateinit on primitives, this default will be overwritten as soon as init is called
    var useSandbox: Boolean = true
        private set

    var callbackUrl: String = ""
        private set

    internal var apiKey: String? = null

    internal lateinit var fileSavePath: String

    internal var allowOfflineMode: Boolean = false
        private set

    /**
     * Initialize the SDK. This must be called before any other SDK methods.
     *
     * @param context A [Context] instance
     * @param config The [Config] to use for the SDK. If not provided, will attempt to load from
     * assets (the recommended approach)
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     * @param enableCrashReporting Whether to enable crash reporting for *ONLY* Smile ID related
     * crashes. This is powered by Sentry, and further details on inner workings can be found in the
     * source docs for [SmileIDCrashReporting]
     * @param okHttpClient An optional [OkHttpClient.Builder] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        context: Context,
        config: Config = Config.fromAssets(context),
        useSandbox: Boolean = false,
        enableCrashReporting: Boolean = true,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) {
        val isInDebugMode = (context.applicationInfo.flags and FLAG_DEBUGGABLE) != 0
        // Plant a DebugTree if there isn't already one (e.g. when Partner also uses Timber)
        if (isInDebugMode && Timber.forest().none { it is Timber.DebugTree }) {
            Timber.plant(Timber.DebugTree())
        }

        SmileID.config = config

        // Enable crash reporting as early as possible (the pre-req is that the config is loaded)
        if (enableCrashReporting) {
            SmileIDCrashReporting.enable(isInDebugMode)
        }
        requestFaceDetectionModuleInstallation(context)

        SmileID.useSandbox = useSandbox
        val url = if (useSandbox) config.sandboxBaseUrl else config.prodBaseUrl

        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(UploadRequestConverterFactory)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(SmileIDService::class.java)

        // Usually looks like: /data/user/0/<package name>/app_SmileID
        fileSavePath = context.getDir("SmileID", MODE_PRIVATE).absolutePath
    }

    @JvmStatic
    fun setAllowOfflineMode(allowOfflineMode: Boolean) {
        SmileID.allowOfflineMode = allowOfflineMode
    }

    /**
     * Initialize the SDK with an API Key. This must be called before any other SDK methods. API
     * keys are different from the auth token in the Config. If this initialization method is used,
     * authToken from [config] need not be used.
     *
     * @param apiKey The API Key to use
     * @param context A [Context] instance
     * @param config The [Config] to use for the SDK. If not provided, will attempt to load from
     * assets (the recommended approach)
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     * @param enableCrashReporting Whether to enable crash reporting for *ONLY* Smile ID related
     * crashes. This is powered by Sentry, and further details on inner workings can be found in the
     * source docs for [SmileIDCrashReporting]
     * @param okHttpClient The [OkHttpClient] to use for the network requests
     */
    @JvmStatic
    @JvmOverloads
    fun initialize(
        apiKey: String,
        context: Context,
        config: Config = Config.fromAssets(context),
        useSandbox: Boolean = false,
        enableCrashReporting: Boolean = true,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ) {
        SmileID.apiKey = apiKey
        initialize(context, config, useSandbox, enableCrashReporting, okHttpClient)
    }

    /**
     * Switches the SDK between the sandbox and production API at runtime. Please note that if the
     * environment is switched while you or the SDK is in the middle of a job (i.e. polling job
     * status), this may cause API errors.
     *
     * @param useSandbox Whether to use the sandbox environment. If false, uses production
     */
    @JvmStatic
    fun setEnvironment(useSandbox: Boolean) {
        SmileID.useSandbox = useSandbox
        val url = if (useSandbox) config.sandboxBaseUrl else config.prodBaseUrl
        retrofit = retrofit.newBuilder().baseUrl(url).build()
        api = retrofit.create(SmileIDService::class.java)
    }

    /**
     * The callback mechanism allows for asynchronous job requests and responses.
     * While the job_status API can be polled to get a result, a better method is to set up a
     * callback url and let the system POST a JSON response.
     *
     * @param callbackUrl The callback url that will be used to asynchronously send results of your
     * job requests
     */
    @JvmStatic
    fun setCallbackUrl(callbackUrl: URL?) {
        SmileID.callbackUrl = callbackUrl?.toString() ?: ""
    }

    /**
     * Retrieves a list of submitted job IDs.
     * This method filters the job IDs to include only those that have been completed (submitted),
     * excluding any pending jobs.
     *
     * @return A list of strings representing the IDs of submitted jobs.
     */
    @JvmStatic
    internal fun getSubmittedJobs(): List<String> = listJobIds(
        includeCompleted = true,
        includePending = false,
    )

    /**
     * Retrieves a list of unsubmitted job IDs.
     * This method filters the job IDs to include only those that are still pending,
     * excluding any completed (submitted) jobs.
     *
     * @return A list of strings representing the IDs of unsubmitted jobs.
     */
    @JvmStatic
    fun getUnSubmittedJobs(): List<String> = listJobIds(
        includeCompleted = false,
        includePending = true,
    )

    /**j
     * Initiates the cleanup process for a single job by its ID.
     * This is a convenience method that wraps the cleanup process, allowing for a single job ID
     * to be specified for cleanup.
     *
     * @param jobId The ID of the job to clean up.
     */
    fun cleanup(jobId: String) = cleanupJobs(jobIds = listOf(jobId))

    /**
     * Initiates the cleanup process for multiple jobs by their IDs.
     * If no IDs are provided, a default cleanup process is initiated that may target
     * specific jobs based on the implementation in com.smileidentity.util.cleanup.
     *
     * @param jobIds An optional list of job IDs to clean up. If null, the method defaults to
     * a predefined cleanup process.
     */
    fun cleanup(jobIds: List<String>? = null) = cleanupJobs(jobIds = jobIds)

    /**
     * Returns an [OkHttpClient.Builder] optimized for low bandwidth conditions. Use it as a
     * starting point if you need to customize an [OkHttpClient] for your own needs
     */
    @JvmStatic
    fun getOkHttpClientBuilder() = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(true)
        callTimeout(60, TimeUnit.SECONDS)
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
        addInterceptor(
            Interceptor { chain: Interceptor.Chain ->
                // Retry on exception (network error) and 5xx
                val request = chain.request()
                for (attempt in 1..3) {
                    try {
                        Timber.v("Smile ID SDK network attempt #$attempt")
                        val response = chain.proceed(request)
                        if (response.code < 500) {
                            return@Interceptor response
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Smile ID SDK network attempt #$attempt failed")
                        // Network failures end up here. These will be retried
                    }
                }
                return@Interceptor chain.proceed(request)
            },
        )
        addInterceptor(
            HttpLoggingInterceptor().apply {
                // This BuildConfig.DEBUG will be false when the SDK is released, regardless of the
                // partner app's debug mode
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            },
        )
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
            .add(SmartSelfieJobResultAdapter)
            .add(DocumentVerificationJobResultAdapter)
            .add(BiometricKycJobResultAdapter)
            .add(EnhancedDocumentVerificationJobResultAdapter)
            .add(JobResultAdapter)
            .add(FallbackEnum.ADAPTER_FACTORY)
            .build()
    }

    /**
     * Request Google Play Services to install the Face Detection Module, if not already installed.
     */
    private fun requestFaceDetectionModuleInstallation(context: Context) {
        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(FaceDetection.getClient())
            .setListener {
                Timber.d(
                    "Face Detection install status: " +
                        "errorCode=${it.errorCode}, " +
                        "installState=${it.installState}, " +
                        "bytesDownloaded=${it.progressInfo?.bytesDownloaded}, " +
                        "totalBytesToDownload=${it.progressInfo?.totalBytesToDownload}",
                )
            }.build()

        ModuleInstall.getClient(context)
            .installModules(moduleInstallRequest)
            .addOnSuccessListener {
                Timber.d("Face Detection install success: ${it.areModulesAlreadyInstalled()}")
            }
            .addOnFailureListener {
                Timber.w(it, "Face Detection install failed")
            }
    }
}
