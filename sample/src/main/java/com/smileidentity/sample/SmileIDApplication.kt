package com.smileidentity.sample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SmileIDApplication : Application() {

    override fun onCreate() {
        super.onCreate()

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
}
