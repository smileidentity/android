package com.smileidentity

import android.os.Build
import com.smileidentity.SmileIDCrashReporting.disable
import io.sentry.Hint
import io.sentry.IScopes
import io.sentry.NoOpScopes
import io.sentry.Scope
import io.sentry.Scopes
import io.sentry.SentryClient
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.UncaughtExceptionHandlerIntegration
import io.sentry.android.timber.SentryTimberIntegration
import io.sentry.protocol.User
import timber.log.Timber

private const val TAG_BRAND = "brand"
private const val TAG_DEBUG_MODE = "debug_mode"
private const val TAG_CPU_ABI = "cpu_abi"
private const val TAG_DEVICE = "device"
private const val TAG_MANUFACTURER = "manufacturer"
private const val TAG_MODEL = "model"
private const val TAG_OS_API_LEVEL = "os_api_level"
private const val TAG_OS_VERSION = "os_version"
private const val TAG_PRODUCT = "product"
private const val TAG_SDK_VERSION = "sdk_version"

/**
 * This class is used to power crash reporting for the Smile ID SDKs. If you would like to opt-out
 * of crash reporting, you can call [disable]. The crash reporting is powered by Sentry. However,
 * you may still use and integrate with Sentry in your own application, as we do not use the global
 * static Sentry class, and we use our own DSN.
 */
object SmileIDCrashReporting {
    private const val SMILE_ID_PACKAGE_PREFIX = "com.smileidentity"
    internal var scopes: IScopes = NoOpScopes.getInstance()

    @JvmStatic
    fun enable(isInDebugMode: Boolean = false) {
        val options = SentryOptions().apply {
            dsn = BuildConfig.SENTRY_DSN
            release = BuildConfig.VERSION_NAME
            isEnableUncaughtExceptionHandler = true
            beforeSend = BeforeSendCallback { event: SentryEvent, _: Hint? ->
                try {
                    if (isEventFromIDE(event)) {
                        // Ignore crashes from IDEs (possible if the SDK is initialized in a Jetpack
                        // Compose preview)
                        return@BeforeSendCallback null
                    }
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
            // Add the Timber integration to capture logs as breadcrumbs
            addIntegration(
                SentryTimberIntegration(
                    minEventLevel = SentryLevel.ERROR,
                    minBreadcrumbLevel = SentryLevel.INFO,
                ),
            )
        }

        // Create scopes
        val globalScope = Scope(options)
        val isolationScope = Scope(options)
        val scope = Scope(options)

        scope.setTag(TAG_BRAND, Build.BRAND)
        scope.setTag(TAG_DEBUG_MODE, isInDebugMode.toString())
        scope.setTag(TAG_CPU_ABI, Build.SUPPORTED_ABIS?.first() ?: "unknown")
        scope.setTag(TAG_DEVICE, Build.DEVICE)
        scope.setTag(TAG_MANUFACTURER, Build.MANUFACTURER)
        scope.setTag(TAG_MODEL, Build.MODEL)
        scope.setTag(TAG_OS_API_LEVEL, Build.VERSION.SDK_INT.toString())
        scope.setTag(TAG_OS_VERSION, Build.VERSION.RELEASE)
        scope.setTag(TAG_PRODUCT, Build.PRODUCT)
        scope.setTag(TAG_SDK_VERSION, BuildConfig.VERSION_NAME)
        try {
            scope.setTag("partner_id", SmileID.config.partnerId)
            scope.setUser(User().apply { id = SmileID.config.partnerId })
        } catch (e: Exception) {
            Timber.w(e, "Error while setting partner_id tag for Sentry")
        }

        globalScope.bindClient(SentryClient(options))

        scopes = Scopes(scope, isolationScope, globalScope, "SmileIDCrashReporting.init")

        // Once this UncaughtExceptionHandler handles the exception, it will pass the exception on
        // to any previously set handlers (if any). If someone registers a new handler after we
        // register ours, and they don't pass it on to us, we may not be notified of the crash.
        val integration = UncaughtExceptionHandlerIntegration()
        options.addIntegration(integration)
        integration.register(scopes, options)
    }

    @JvmStatic
    fun disable() {
        scopes.options.isEnableUncaughtExceptionHandler = false
        for (it in scopes.options.integrations) {
            if (it is UncaughtExceptionHandlerIntegration) {
                it.close()
            }
        }
        scopes = NoOpScopes.getInstance()
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

    /**
     * Determine whether the event was caused by an IDE. This is done heuristically by checking the
     * JVM runtime tag (which is likely "JetBrains s.r.o. 17.0.6", but it is possible for the
     * developer to change the runtime. We also check the brand and device. If there happens to be
     * an actual brand called "studio" or an actual device called "layoutlib" then we would
     * ignore exceptions from those devices
     */
    private fun isEventFromIDE(event: SentryEvent): Boolean =
        "JetBrains" in (event.getTag("runtime") ?: "") ||
            event.getTag(TAG_BRAND) == "studio" ||
            event.getTag(TAG_DEVICE) == "layoutlib"
}
