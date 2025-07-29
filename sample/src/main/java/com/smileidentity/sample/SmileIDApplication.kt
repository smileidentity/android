package com.smileidentity.sample

import android.app.Application
import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.smileidentity.sample.repo.DataStoreRepository
import timber.log.Timber

class SmileIDApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        appContext = this
        Timber.plant(Timber.DebugTree())
        setupPlayIntegrity()

        // *****Note to Partners*****
        // The line below is how you should initialize the SmileID SDK
        // SmileID.initialize(this)
    }

    fun setupPlayIntegrity() {
        val standardIntegrityManager = IntegrityManagerFactory.createStandard(appContext)
        standardIntegrityManager.prepareIntegrityToken(
            StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
//                .setCloudProjectNumber()
                .build()
        ).addOnSuccessListener { tokenProvider ->
            val integrityTokenProvider = tokenProvider
            // Use integrityTokenProvider as needed
            Timber.d("Successfully prepared integrity token: ${integrityTokenProvider}")
        }.addOnFailureListener { exception ->
            Timber.d(exception, "Failed to prepare integrity token")
            // Handle the failure to prepare the integrity to
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
