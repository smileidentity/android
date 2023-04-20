package com.smileidentity.sample

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.smileidentity.SmileID
import com.smileidentity.SmileID.getOkHttpClientBuilder
import timber.log.Timber

class SmileIDApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        val chucker = ChuckerInterceptor.Builder(this).build()
        SmileID.initialize(
            context = this,
            useSandbox = true,
            enableCrashReporting = !BuildConfig.DEBUG,
            okHttpClient = getOkHttpClientBuilder().addInterceptor(chucker).build(),
        )
    }
}
