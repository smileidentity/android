package com.smileidentity.sample

import android.app.Application
import android.content.Context
import com.smileidentity.sample.repo.DataStoreRepository
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SmileIDApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        appContext = this // todo remove this
        setupTimber()

        // *****Note to Partners*****
        // The line below is how you should initialize the SmileID SDK
        // SmileID.initialize(this)
    }

    private fun setupTimber() = Timber.plant(
        object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String {
                return super.createStackElementTag(element) + ":" + element.lineNumber
            }
        },
    )

    // todo remove this
    companion object {
        /**
         * This exists only for usage with [DataStoreRepository] since we currently do not use DI.
         * Do not use it anywhere else.
         */
        lateinit var appContext: Context
    }
}
