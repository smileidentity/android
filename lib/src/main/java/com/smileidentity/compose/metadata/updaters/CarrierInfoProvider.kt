package com.smileidentity.compose.metadata.updaters

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import com.smileidentity.compose.metadata.models.MetadataKey
import com.smileidentity.models.v2.metadata.MetadataEntry
import com.smileidentity.compose.metadata.MetadataProvider

class CarrierInfoProvider(context: Context) : MetadataProvider {
    private val packageManager = context.packageManager
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

    /**
     * Checks if the device supports telephony subscriptions.
     * This ensures the device has SIM support and can manage cellular network subscriptions.
     */
    private fun hasTelephonySubscription(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_SUBSCRIPTION)
        } else {
            packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        }
    }

    /**
     * Gets the carrier name.
     * Returns "unknown" if it can't access the carrier name.
     */
    private fun getCarrierName(): String {
        telephonyManager?.let {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API 28+
                val carrierName = telephonyManager.simCarrierIdName
                carrierName?.toString() ?: "unknown"
            } else {
                val legacyCarrierName = telephonyManager.networkOperatorName
                legacyCarrierName.takeIf { !it.isNullOrEmpty() } ?: "unknown"
            }
        }
        return "unknown"
    }

    override fun collectMetadata(): Map<MetadataKey, MutableList<MetadataEntry>> {
        val carrierInfo = if (hasTelephonySubscription()) {
            getCarrierName()
        } else {
            "unknown"
        }
        return mapOf(
            MetadataKey.CarrierInfo to mutableListOf(MetadataEntry(carrierInfo)),
        )
    }
}
