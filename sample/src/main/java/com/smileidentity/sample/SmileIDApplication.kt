package com.smileidentity.sample

import android.app.Application
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.processphoenix.ProcessPhoenix
import com.smileidentity.SmileID
import com.smileidentity.SmileID.getOkHttpClientBuilder
import com.smileidentity.sample.repo.DataStoreRepository
import timber.log.Timber
import java.io.EOFException
import java.io.FileNotFoundException

class SmileIDApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // if (ProcessPhoenix.isPhoenixProcess(this)) {
        //     return // skip initialization for Phoenix process
        // }
        appContext = this
        Timber.plant(Timber.DebugTree())
        val chucker = ChuckerInterceptor.Builder(this).build()
        try {
            SmileID.initialize(
                context = this,
                useSandbox = true,
                enableCrashReporting = !BuildConfig.DEBUG,
                okHttpClient = getOkHttpClientBuilder().addInterceptor(chucker).build(),
            )
        } catch (e: EOFException) {
            // EOFException - if the json is not correct
            Timber.e(e)
        } catch (e: FileNotFoundException) {
            // FileNotFoundException - if the config file doesn't exist
            Timber.e(e)
        }
    }

    companion object {
        /**
         * This exists only for usage with [DataStoreRepository] since we currently do not use DI.
         * Do not use it anywhere else.
         */
        lateinit var appContext: Context
    }
}
