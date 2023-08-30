package com.smileidentity.sample

import android.app.Application
import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.smileidentity.sample.repo.DataStoreRepository
import timber.log.Timber

class SmileIDApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        appContext = this
        Timber.plant(Timber.DebugTree())

        // *****Note to Partners*****
        // The line below is how you should initialize the SmileID SDK
        // SmileID.initialize(this)

        // Initialize bar code scanner
        initializeBarcodeScanner()
    }

    private fun initializeBarcodeScanner() {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
            )
            .enableAutoZoom()
            .build()
    }

    companion object {
        /**
         * This exists only for usage with [DataStoreRepository] since we currently do not use DI.
         * Do not use it anywhere else.
         */
        lateinit var appContext: Context
    }
}
