package com.smileidentity.ui

import android.os.Build
import io.sentry.Hint
import io.sentry.Hub
import io.sentry.IHub
import io.sentry.NoOpHub
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.UncaughtExceptionHandlerIntegration

/**
 * This class is used to enable crash reporting for the Smile Identity SDKs. You must explicitly
 * opt-in by calling `SmileIdentityCrashReporting.enable()` in your Application's `onCreate`.
 * If you do not call this method, no crash reporting will be enabled. If you would like to opt-out
 * of crash reporting, you can call `SmileIdentityCrashReporting.disable()`. The crash reporting is
 * powered by Sentry. However, you may still use and integrate with Sentry in your own application,
 * as we do not use the global static Sentry class, and we use our own DSN.
 */
object SmileIdentityCrashReporting {
    internal var hub: IHub = NoOpHub.getInstance()

    @JvmStatic
    fun enable() {
        val options = SentryOptions().apply {
            dsn = BuildConfig.SENTRY_DSN
            isEnableUncaughtExceptionHandler = true
            beforeSend = BeforeSendCallback { event: SentryEvent, _: Hint? ->
                try {
                    // Only report crashes from the SmileIdentity SDK
                    if (isCausedBySmileSDK(event.throwable)) {
                        return@BeforeSendCallback event
                    }
                } catch (e: Exception) {
                    // Catch all exceptions to prevent Sentry itself from crashing, in case there is
                    // a bug in our crash reporting code.
                    println("Error while processing crash report for Sentry: $e")
                }
                return@BeforeSendCallback null
            }
        }

        hub = Hub(options).apply {
            setTag("sdk_version", BuildConfig.VERSION_NAME)
            setTag("build_type", BuildConfig.BUILD_TYPE) // Distinguish between debug and release
            setTag("os_version", Build.VERSION.RELEASE)
            setTag("os_api_level", Build.VERSION.SDK_INT.toString())
            setTag("cpu_abi", Build.SUPPORTED_ABIS?.first() ?: "unknown")
            setTag("product", Build.PRODUCT)
            setTag("manufacturer", Build.MANUFACTURER)
            setTag("brand", Build.BRAND)
            setTag("device", Build.DEVICE)
            setTag("model", Build.MODEL)
        }

        // Once this UncaughtExceptionHandler handles the exception, it will pass the exception on
        // to any previously set handlers (if any). If someone registers a new handler after we
        // register ours, and they don't pass it on to us, we may not be notified of the crash.
        val integration = UncaughtExceptionHandlerIntegration()
        options.addIntegration(integration)
        integration.register(hub, options)
    }

    @JvmStatic
    fun disable() {
        hub.options.isEnableUncaughtExceptionHandler = false
        hub.options.integrations.forEach {
            if (it is UncaughtExceptionHandlerIntegration) {
                it.close()
            }
        }
        hub = NoOpHub.getInstance()
    }

    private const val SMILE_IDENTITY_PACKAGE_PREFIX = "com.smileidentity"

    /**
     * Checks whether the provided throwable involves Smile Identity SDK. This is done by checking
     * the stack trace of the throwable and its causes.
     *
     * @param throwable The throwable to check.
     * @return True if the throwable was caused by a Smile Identity SDK, false otherwise.
     */
    private fun isCausedBySmileSDK(throwable: Throwable?): Boolean {
        if (throwable == null) {
            return false
        }
        if (throwable.toString().contains(SMILE_IDENTITY_PACKAGE_PREFIX)) {
            println("Throwable involves Smile Identity SDK")
            return true
        }

        // Check if any item of the stack trace is from a Smile Identity SDK
        throwable.stackTrace.forEach {
            if (it.className.contains(SMILE_IDENTITY_PACKAGE_PREFIX)) {
                println("Found a class from Smile Identity SDK: ${it.className}")
                return true
            }
        }

        // If this throwable is the root cause, getCause will return null. In which case, the
        // recursive base case will be reached and false will be returned.
        return isCausedBySmileSDK(throwable.cause)
    }
}
