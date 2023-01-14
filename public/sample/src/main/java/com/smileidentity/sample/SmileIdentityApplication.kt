package com.smileidentity.sample

import android.app.Application
import com.google.android.material.color.DynamicColors
import timber.log.Timber

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        // SmileIdentityCrashReporting.enable()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
