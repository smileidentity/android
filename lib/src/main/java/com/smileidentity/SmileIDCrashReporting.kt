package com.smileidentity

import android.os.Build
import com.smileidentity.SmileIDCrashReporting.disable
import io.sentry.Hint
import io.sentry.Hub
import io.sentry.IHub
import io.sentry.NoOpHub
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.UncaughtExceptionHandlerIntegration
import io.sentry.protocol.User
import timber.log.Timber

/**
 * This class is used to power crash reporting for the Smile ID SDKs. If you would like to opt-out
 * of crash reporting, you can call [disable]. The crash reporting is powered by Sentry. However,
 * you may still use and integrate with Sentry in your own application, as we do not use the global
 * static Sentry class, and we use our own DSN.
 */
object SmileIDCrashReporting {
    private const val SMILE_ID_PACKAGE_PREFIX = "com.smileidentity"
    internal var hub: IHub = NoOpHub.getInstance()

    @JvmStatic
    fun enable(isInDebugMode: Boolean = false) {
        val options = SentryOptions().apply {
            dsn = BuildConfig.SENTRY_DSN
            isEnableUncaughtExceptionHandler = true
            beforeSend = BeforeSendCallback { event: SentryEvent, _: Hint? ->
                try {
                    // Only report crashes from the SmileID SDK
                    if (isCausedBySmileID(event.throwable)) {
                        return@BeforeSendCallback event
                    }
                } catch (e: Exception) {
                    // Catch all exceptions to prevent Sentry itself from crashing, in case there is
                    // a bug in our crash reporting code.
                    Timber.e(e, "Error while processing crash report for Sentry")
                }
                return@BeforeSendCallback null
            }
        }

        hub = Hub(options).apply {
            setTag("brand", Build.BRAND)
            setTag("debug_mode", isInDebugMode.toString())
            setTag("cpu_abi", Build.SUPPORTED_ABIS?.first() ?: "unknown")
            setTag("device", Build.DEVICE)
            setTag("manufacturer", Build.MANUFACTURER)
            setTag("model", Build.MODEL)
            setTag("os_api_level", Build.VERSION.SDK_INT.toString())
            setTag("os_version", Build.VERSION.RELEASE)
            setTag("product", Build.PRODUCT)
            setTag("sdk_version", BuildConfig.VERSION_NAME)
            try {
                setTag("partner_id", SmileID.config.partnerId)
                setUser(User().apply { id = SmileID.config.partnerId })
            } catch (e: Exception) {
                // Ignore
                Timber.w(e, "Error while setting partner_id tag for Sentry")
            }
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
        for (it in hub.options.integrations) {
            if (it is UncaughtExceptionHandlerIntegration) {
                it.close()
            }
        }
        hub = NoOpHub.getInstance()
    }

    /**
     * Checks whether the provided throwable involves Smile ID SDK. This is done by checking the
     * stack trace of the throwable and its causes.
     *
     * @param throwable The throwable to check.
     * @return True if the throwable was caused by a Smile ID SDK, false otherwise.
     */
    private fun isCausedBySmileID(throwable: Throwable?): Boolean {
        if (throwable == null) {
            return false
        }
        if (throwable.toString().contains(SMILE_ID_PACKAGE_PREFIX)) {
            Timber.d("Throwable involves the Smile ID SDK")
            return true
        }

        // Check if any item of the stack trace is from a Smile ID SDK
        throwable.stackTrace.forEach {
            if (it.className.contains(SMILE_ID_PACKAGE_PREFIX)) {
                Timber.d("Found a class from the Smile ID SDK: ${it.className}")
                return true
            }
        }

        // If this throwable is the root cause, getCause will return null. In which case, the
        // recursive base case will be reached and false will be returned.
        return isCausedBySmileID(throwable.cause)
    }
}
