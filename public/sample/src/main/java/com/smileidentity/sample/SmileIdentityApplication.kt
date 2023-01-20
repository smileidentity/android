package com.smileidentity.sample

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.android.material.color.DynamicColors
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.SmileIdentity.getOkHttpClientBuilder
import com.smileidentity.ui.core.init
import timber.log.Timber

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        DynamicColors.applyToActivitiesIfAvailable(this)
        @Suppress("DEPRECATION")
        SmileIdentity.init(
            context = this,
            useSandbox = BuildConfig.DEBUG,
            enableCrashReporting = BuildConfig.DEBUG,
            okHttpClient = getOkHttpClientBuilder()
                .addInterceptor(ChuckerInterceptor(this))
                .build()
        )
    }
}
