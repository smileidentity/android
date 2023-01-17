package com.smileidentity.sample

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.models.Config
import timber.log.Timber

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        DynamicColors.applyToActivitiesIfAvailable(this)
        // SmileIdentityCrashReporting.enable()
        val config = Config(
            baseUrl = "https://api.smileidentity.com/v1/",
            sandboxBaseUrl = "https://testapi.smileidentity.com/v1/",
            partnerId = "2343",
        )
        SmileIdentity.init(
            apiKey = BuildConfig.SMILE_IDENTITY_API_KEY,
            config = config,
            useSandbox = true,
        )
    }
}
