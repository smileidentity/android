package com.smileidentity.ui.core

import android.content.Context
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.models.Config
import okhttp3.OkHttpClient

/**
 * Initialize the SDK. This must be called before any other SDK methods.
 *
 * @param context A [Context] instance which will be used to load the config file from assets
 * @param useSandbox Whether to use the sandbox environment. If false, uses production
 * @param enableCrashReporting Whether to enable crash reporting for *ONLY* Smile
 * Identity related crashes. This is powered by Sentry, and further details on inner workings can be
 * found in the source docs for [SmileIdentityCrashReporting]
 * @param okHttpClient An optional [OkHttpClient.Builder] to use for the network requests
 */
@JvmOverloads
fun SmileIdentity.init(
    context: Context,
    useSandbox: Boolean = false,
    enableCrashReporting: Boolean = false,
    okHttpClient: OkHttpClient = getOkHttpClientBuilder().build(),
) {
    if (enableCrashReporting) {
        SmileIdentityCrashReporting.enable()
    }
    init(
        config = Config.fromAssets(context),
        useSandbox = useSandbox,
        okHttpClient = okHttpClient,
    )
    // enabled after init to allow partner_id to be fetched from lateinit config.
    if (enableCrashReporting) {
        SmileIdentityCrashReporting.hub.setTag("partner_id", config.partnerId)
    }
}
