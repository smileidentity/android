package com.smileidentity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import android.provider.Settings.Secure
import androidx.core.content.edit
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.sdkinternal.MlKitContext
import com.google.mlkit.vision.face.FaceDetection
import com.serjltt.moshi.adapters.FallbackEnum
import com.smileidentity.SmileID.initialize
import com.smileidentity.metadata.models.WrapperSdkName
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.Config
import com.smileidentity.models.IdInfo
import com.smileidentity.models.JobType
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import com.smileidentity.networking.BiometricKycJobResultAdapter
import com.smileidentity.networking.DocumentVerificationJobResultAdapter
import com.smileidentity.networking.EnhancedDocumentVerificationJobResultAdapter
import com.smileidentity.networking.FileAdapter
import com.smileidentity.networking.FileNameAdapter
import com.smileidentity.networking.GzipRequestInterceptor
import com.smileidentity.networking.JobResultAdapter
import com.smileidentity.networking.JobTypeAdapter
import com.smileidentity.networking.MetadataAdapter
import com.smileidentity.networking.PartnerParamsAdapter
import com.smileidentity.networking.SmartSelfieJobResultAdapter
import com.smileidentity.networking.SmileHeaderAuthInterceptor
import com.smileidentity.networking.SmileHeaderMetadataInterceptor
import com.smileidentity.networking.SmileIDService
import com.smileidentity.networking.StringifiedBooleanAdapter
import com.smileidentity.networking.UploadRequestConverterFactory
import com.smileidentity.networking.ValueJsonAdapter
import com.smileidentity.networking.asDocumentBackImage
import com.smileidentity.networking.asDocumentFrontImage
import com.smileidentity.networking.asLivenessImage
import com.smileidentity.networking.asSelfieImage
import com.smileidentity.security.interceptor.SmileSecurityInterceptor
import com.smileidentity.util.AUTH_REQUEST_FILE
import com.smileidentity.util.FileType
import com.smileidentity.util.PREP_UPLOAD_REQUEST_FILE
import com.smileidentity.util.UPLOAD_REQUEST_FILE
import com.smileidentity.util.cleanupJobs
import com.smileidentity.util.doGetSubmittedJobs
import com.smileidentity.util.doGetUnsubmittedJobs
import com.smileidentity.util.getExceptionHandler
import com.smileidentity.util.getFileByType
import com.smileidentity.util.getFilesByType
import com.smileidentity.util.getSmileTempFile
import com.smileidentity.util.handleOfflineJobFailure
import com.smileidentity.util.moveJobToSubmitted
import com.smileidentity.util.toSmileIDException
import com.squareup.moshi.Moshi
import io.sentry.Breadcrumb
import io.sentry.SentryLevel
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber

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

    internal var allowOfflineMode: Boolean = false
        private set

    var callbackUrl: String = ""
        private set

    internal var apiKey: String? = null

    internal lateinit var fileSavePath: String

    /**
     * We need this to not break the old sdk implementation
     *
     * We will remove this in the next breaking version (v11)
     */
    internal lateinit var oldFileSavePath: String
    internal var fingerprint = ""

    internal var wrapperSdkName: WrapperSdkName? = null
        private set
    internal var wrapperSdkVersion: String? = null
        private set

    internal var sdkLaunchCount = 0
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
    // "Using device identifiers is not recommended other than for high value fraud prevention"
    @SuppressLint("HardwareIds")
    @JvmStatic
    @JvmOverloads
    fun initialize(
        context: Context,
        config: Config = Config.fromAssets(context),
        useSandbox: Boolean = false,
        enableCrashReporting: Boolean = true,
        okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
    ): Deferred<Result<Unit>> {
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

        SmileID.useSandbox = useSandbox
        val url = if (useSandbox) config.testLambdaUrl else config.prodLambdaUrl

        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(UploadRequestConverterFactory)
            // Needed for String form data. Otherwise the Moshi adapter adds extraneous quotations
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(SmileIDService::class.java)

        // Usually looks like: /data/data/0/<package-name>/files/SmileID
        fileSavePath = File("${context.filesDir.absolutePath}/SmileID").absolutePath

        // todo :: will remove this in v11 (breaking change)
        // Usually looks like: /data/user/0/<package name>/app_SmileID
        oldFileSavePath = context.getDir("SmileID", MODE_PRIVATE).absolutePath

        // ANDROID_ID may be null. Since Android 8, each app has a different value
        Secure.getString(context.contentResolver, Secure.ANDROID_ID)?.let { fingerprint = it }

        trackSdkLaunchCount(context)

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        return scope.async {
            runCatching {
                requestFaceDetectionModuleInstallation(context)
            }.onFailure { throwable ->
                Timber.d("Face Detection Module Exception: ${throwable.message}")
            }
        }
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
    ): Deferred<Result<Unit>> {
        SmileID.apiKey = apiKey
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        return scope.async {
            initialize(
                context = context,
                config = config,
                useSandbox = useSandbox,
                enableCrashReporting = enableCrashReporting,
                okHttpClient = okHttpClient,
            ).await()
        }
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
     * Sets the state of offline mode for the SDK.
     *
     * This function enables or disables the SDK's ability to operate in offline mode,
     * where it can continue functioning without an active internet connection. When offline mode
     * is enabled (allowOfflineMode = true), the SDK will attempt to use capture and cache
     * images in local file storage and will not attempt to submit the job. Conversely, when offline
     * mode is disabled (allowOfflineMode = false), the application will require an active internet
     * connection for all operations that involve data fetching or submission.
     *
     * @param allowOfflineMode A Boolean value indicating whether offline mode should be enabled (true)
     *                         or disabled (false).
     */
    @JvmStatic
    fun setAllowOfflineMode(allowOfflineMode: Boolean) {
        SmileID.allowOfflineMode = allowOfflineMode
    }

    /**
     * Retrieves a list of submitted job IDs.
     * This method filters the job IDs to include only those that have been completed (submitted),
     * excluding any pending jobs.
     *j
     * @return A list of strings representing the IDs of submitted jobs.
     */
    @JvmStatic
    fun getSubmittedJobs(): List<String> = doGetSubmittedJobs()

    /**
     * Retrieves a list of unsubmitted job IDs.
     * This method filters the job IDs to include only those that are still pending,
     * excluding any completed (submitted) jobs.
     *
     * @return A list of strings representing the IDs of unsubmitted jobs.
     */
    @JvmStatic
    fun getUnsubmittedJobs(): List<String> = doGetUnsubmittedJobs()

    /**
     * Initiates the cleanup process for a single job by its ID.
     * This is a convenience method that wraps the cleanup process, allowing for a single job ID
     * to be specified for cleanup.
     *
     * @param jobId The ID of the job to clean up.Helpful methods for obtaining job
     *  *              IDs include: [doGetSubmittedJobs] [doGetUnsubmittedJobs]
     */
    @JvmStatic
    fun cleanup(jobId: String) = cleanupJobs(jobIds = listOf(jobId))

    /**
     * Initiates the cleanup process for multiple jobs by their IDs.
     * If no IDs are provided, a default cleanup process is initiated that may target
     * specific jobs based on the implementation in com.smileidentity.util.cleanup.
     *
     * @param jobIds An optional list of job IDs to clean up. If null, the method defaults to
     * a predefined cleanup process.  Helpful methods for obtaining
     * job IDs include:[doGetSubmittedJobs], [doGetUnsubmittedJobs]
     */
    @JvmStatic
    fun cleanup(jobIds: List<String>? = null) = cleanupJobs(jobIds = jobIds)

    /**
     * Submits a previously captured job to SmileID for processing.
     *
     * @param jobId The unique identifier for the job to be submitted. This ID should be obtained
     *              through the appropriate SmileID service mechanism and is used to track and
     *              manage the job within SmileID's processing system. Helpful methods for
     *              obtaining job  IDs include: [getSubmittedJobs] [getUnsubmittedJobs]
     *
     * Usage:
     * To use this function, ensure you are calling it from a coroutine scope or
     * another suspend function. For example, in a coroutine scope:
     *
     * ```kotlin
     * coroutineScope {
     *     SmileID.submitJob("your_job_id")
     * }
     * ```
     * Note: Ensure that the jobId provided is valid and that your environment is properly set up
     * to handle potential network responses, including success, failure, or error cases.
     */
    @JvmStatic
    fun submitJob(
        jobId: String,
        deleteFilesOnSuccess: Boolean = true,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        exceptionHandler: ((Throwable) -> Unit)? = null,
    ): Job = scope.launch(
        getExceptionHandler { throwable ->
            handleOfflineJobFailure(jobId, throwable, exceptionHandler)
        },
    ) {
        val jobIds = doGetUnsubmittedJobs()
        if (jobId !in jobIds) {
            Timber.v("Invalid jobId or not found")
            throw IllegalArgumentException("Invalid jobId or not found")
        }
        val authRequestJsonString = getSmileTempFile(
            folderName = jobId,
            fileName = AUTH_REQUEST_FILE,
            isUnsubmitted = true,
        ).useLines { it.joinToString("\n") }
        val authRequest = moshi.adapter(AuthenticationRequest::class.java)
            .fromJson(authRequestJsonString)?.apply {
                authToken = config.authToken
            }
            ?: run {
                Timber.v(
                    "Error decoding AuthenticationRequest JSON to class: %s",
                    authRequestJsonString,
                )
                throw IllegalArgumentException("Invalid jobId information")
            }

        val authResponse = api.authenticate(authRequest)

        val prepUploadRequestJsonString = getSmileTempFile(
            jobId,
            PREP_UPLOAD_REQUEST_FILE,
            true,
        ).useLines { it.joinToString("\n") }
        val savedPrepUploadRequest = moshi.adapter(PrepUploadRequest::class.java)
            .fromJson(prepUploadRequestJsonString)
            ?: run {
                Timber.v(
                    "Error decoding PrepUploadRequest JSON to class: %s",
                    prepUploadRequestJsonString,
                )
                throw IllegalArgumentException("Invalid jobId information")
            }

        val prepUploadRequest = savedPrepUploadRequest.copy(
            timestamp = authResponse.timestamp,
            signature = authResponse.signature,
        )

        val prepUploadResponse = runCatching {
            api.prepUpload(prepUploadRequest)
        }.recoverCatching { throwable ->
            when {
                throwable is HttpException -> {
                    val smileIDException = throwable.toSmileIDException()
                    if (smileIDException.details.code == "2215") {
                        api.prepUpload(prepUploadRequest.copy(retry = true))
                    } else {
                        throw smileIDException
                    }
                }

                else -> {
                    throw throwable
                }
            }
        }.getOrThrow()

        val selfieFileResult = getFileByType(
            folderName = jobId,
            fileType = FileType.SELFIE,
            submitted = false,
        )
        val livenessFilesResult = getFilesByType(
            folderName = jobId,
            fileType = FileType.LIVENESS,
            submitted = false,
        )
        val documentFrontFileResult =
            getFileByType(folderName = jobId, fileType = FileType.DOCUMENT_FRONT, submitted = false)
        val documentBackFileResult =
            getFileByType(folderName = jobId, fileType = FileType.DOCUMENT_BACK, submitted = false)

        val selfieImageInfo = selfieFileResult?.asSelfieImage()
        val livenessImageInfo = livenessFilesResult.map { it.asLivenessImage() }
        val frontImageInfo = documentFrontFileResult?.asDocumentFrontImage()
        val backImageInfo = documentBackFileResult?.asDocumentBackImage()

        var idInfo: IdInfo? = null
        if (authRequest.jobType == JobType.BiometricKyc ||
            authRequest.jobType == JobType.DocumentVerification ||
            authRequest.jobType == JobType.EnhancedDocumentVerification
        ) {
            val uploadRequestJson = getSmileTempFile(
                jobId,
                UPLOAD_REQUEST_FILE,
                true,
            ).useLines { it.joinToString("\n") }
            val savedUploadRequestJson = moshi.adapter(UploadRequest::class.java)
                .fromJson(uploadRequestJson)
                ?: run {
                    Timber.v(
                        "Error decoding UploadRequest JSON to class: %s",
                        uploadRequestJson,
                    )
                    throw IllegalArgumentException("Invalid jobId information")
                }
            idInfo = savedUploadRequestJson.idInfo
        }

        val uploadRequest = UploadRequest(
            images = listOfNotNull(
                frontImageInfo,
                backImageInfo,
                selfieImageInfo,
            ) +
                livenessImageInfo,
            idInfo = idInfo,
        )
        api.upload(prepUploadResponse.uploadUrl, uploadRequest)
        if (deleteFilesOnSuccess) {
            cleanup(jobId)
        } else {
            val copySuccess = moveJobToSubmitted(jobId)
            if (!copySuccess) {
                Timber.w("Failed to move job $jobId to complete")
                SmileIDCrashReporting.scopes.addBreadcrumb(
                    Breadcrumb().apply {
                        category = "Offline Mode"
                        message = "Failed to move job $jobId to complete"
                        level = SentryLevel.INFO
                    },
                )
            }
        }
        Timber.d("Upload finished")
    }

    /**
     * Returns an [OkHttpClient.Builder] optimized for low bandwidth conditions. Use it as a
     * starting point if you need to customize an [OkHttpClient] for your own needs
     */
    @JvmStatic
    fun getOkHttpClientBuilder() = OkHttpClient.Builder().apply {
        callTimeout(timeout = 120, TimeUnit.SECONDS)
        connectTimeout(timeout = 60, unit = TimeUnit.SECONDS)
        readTimeout(timeout = 60, unit = TimeUnit.SECONDS)
        writeTimeout(timeout = 60, unit = TimeUnit.SECONDS)
        addInterceptor(interceptor = SmileHeaderMetadataInterceptor)
        addInterceptor(interceptor = SmileHeaderAuthInterceptor)
        addInterceptor(interceptor = SmileSecurityInterceptor)
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

        // NB! This is the last interceptor so that the logging interceptors come before the request
        //  is gzipped
        // We gzip all requests by default. While supported for Smile ID, it may not be supported by
        //  all servers
        addInterceptor(GzipRequestInterceptor())
    }

    /**
     * Create the [Moshi] instance used by the SDK. It is declared here instead of [initialize]
     * because [Moshi] needs to already be initialized when the UI module attempts to read the
     * config JSON file directly into a [Config], which needs to happen as a prerequisite to the
     * [initialize] call
     */
    private fun initMoshi(): Moshi = Moshi.Builder()
        .add(JobTypeAdapter)
        .add(PartnerParamsAdapter)
        .add(StringifiedBooleanAdapter)
        .add(MetadataAdapter)
        .add(ValueJsonAdapter)
        .add(FileNameAdapter)
        .add(FileAdapter)
        .add(SmartSelfieJobResultAdapter)
        .add(DocumentVerificationJobResultAdapter)
        .add(BiometricKycJobResultAdapter)
        .add(EnhancedDocumentVerificationJobResultAdapter)
        .add(JobResultAdapter)
        .add(FallbackEnum.ADAPTER_FACTORY)
        .build()

    /**
     * Request Google Play Services to install the Face Detection Module, if not already installed.
     */
    @Suppress("ktlint:standard:max-line-length")
    private suspend fun requestFaceDetectionModuleInstallation(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                // see: https://github.com/googlesamples/mlkit/issues/264
                MlKitContext.initializeIfNeeded(context)

                val moduleInstallRequest = ModuleInstallRequest.newBuilder()
                    .addApi(FaceDetection.getClient())
                    .setListener {
                        val message = "Face Detection install status: " +
                            "errorCode=${it.errorCode}, " +
                            "installState=${it.installState}, " +
                            "bytesDownloaded=${it.progressInfo?.bytesDownloaded}, " +
                            "totalBytesToDownload=${it.progressInfo?.totalBytesToDownload}"
                        Timber.d(message)
                        SmileIDCrashReporting.scopes.addBreadcrumb(message)
                    }.build()

                val installTask =
                    ModuleInstall.getClient(context).installModules(moduleInstallRequest)

                Tasks.await(installTask)

                if (installTask.isSuccessful) {
                    Timber.d(
                        "Face Detection install success: " +
                            "${installTask.result.areModulesAlreadyInstalled()}",
                    )
                } else {
                    val exception = installTask.exception
                        ?: Exception(
                            "An error occurred during Face Detection module installation." +
                                "See the guide here to use the bundled version " +
                                "https://developers.google.com/ml-kit/vision/face-detection/android",
                        )
                    Timber.w(exception, "Face Detection install failed")
                    SmileIDCrashReporting.scopes.addBreadcrumb("Face Detection install failed")
                    throw exception
                }
            } catch (exception: Exception) {
                Timber.e(exception, "Error during Face Detection module installation")
                SmileIDCrashReporting.scopes.addBreadcrumb(
                    "Error during Face Detection module installation: ${exception.message}",
                )
                throw exception
            }
        }
    }

    private fun trackSdkLaunchCount(context: Context) {
        try {
            val sharedPreferences = context.getSharedPreferences("SmileIDPrefs", MODE_PRIVATE)
            val key = "SmileID.SDKLaunchCount"
            val currentCount = sharedPreferences.getInt(key, 0)
            val newCount = currentCount + 1
            sharedPreferences.edit { putInt(key, newCount) }

            sdkLaunchCount = newCount
        } catch (e: Exception) {
            // do nothing, it's just metadata
        }
    }

    /**
     * Sets the name and version of a x-platform sdk that wraps the native sdk. This is an internal
     * function and should not be used by partner developers.
     *
     * @param name The name of the x-platform sdk that wraps the native sdk.
     * @param version The version of the x-platform sdk that wraps the native sdk.
     */
    fun setWrapperInfo(name: WrapperSdkName, version: String) {
        wrapperSdkName = name
        wrapperSdkVersion = version
    }
}
