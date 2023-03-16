package com.smileidentity.sample

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.smileidentity.networking.SmileIdentity
import com.smileidentity.networking.SmileIdentity.getOkHttpClientBuilder
import com.smileidentity.ui.core.initialize
import timber.log.Timber

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        val chucker = ChuckerInterceptor.Builder(this).build()
        SmileIdentity.initialize(
            context = this,
            useSandbox = BuildConfig.DEBUG,
            enableCrashReporting = false,
            okHttpClient = getOkHttpClientBuilder().addInterceptor(chucker).build(),
        )
    }
}
