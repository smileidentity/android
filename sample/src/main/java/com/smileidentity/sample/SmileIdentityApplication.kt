package com.smileidentity.sample

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.smileidentity.SmileIdentity
import com.smileidentity.SmileIdentity.getOkHttpClientBuilder
import timber.log.Timber

class SmileIdentityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        val chucker = ChuckerInterceptor.Builder(this).build()
        SmileIdentity.initialize(
            context = this,
            useSandbox = true,
            enableCrashReporting = !BuildConfig.DEBUG,
            okHttpClient = getOkHttpClientBuilder().addInterceptor(chucker).build(),
        )
    }
}
