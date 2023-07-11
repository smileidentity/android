package com.smileidentity.sample

import android.app.Application
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.smileidentity.SmileID
import com.smileidentity.SmileID.getOkHttpClientBuilder
import com.smileidentity.sample.repo.DataStoreRepository
import timber.log.Timber

class SmileIDApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        Timber.plant(Timber.DebugTree())
        val chucker = ChuckerInterceptor.Builder(this).build()
        SmileID.initialize(
            context = this,
            useSandbox = true,
            enableCrashReporting = !BuildConfig.DEBUG,
            okHttpClient = getOkHttpClientBuilder().addInterceptor(chucker).build(),
        )
    }

    companion object {
        /**
         * This exists only for usage with [DataStoreRepository] since we currently do not use DI.
         * Do not use it anywhere else.
         */
        lateinit var appContext: Context
    }
}
