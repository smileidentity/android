package com.smileidentity.sample

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.ui.core.init
import timber.log.Timber

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        DynamicColors.applyToActivitiesIfAvailable(this)
        SmileIdentity.init(
            context = this,
            useSandbox = false,
            enableCrashReporting = true,
        )
    }
}
