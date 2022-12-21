package com.smileidentity.sample

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.smileidentity.ui.SmileIdentityCrashReporting

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SmileIdentityCrashReporting.enable()
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
