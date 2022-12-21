package com.smileidentity.sample

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.smileidentity.ui.core.SmileIdentityCrashReporting
import timber.log.Timber

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        SmileIdentityCrashReporting.enable()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
