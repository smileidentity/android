package com.smileidentity.metadata.updaters

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.Metadatum.CarrierInfo
import com.smileidentity.metadata.updateOrAddBy

/**
 * A manager that updates metadata with the carrier information.
 */
internal class CarrierInfoMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.CarrierInfo.key

    private val packageManager = context.packageManager
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

    init {
        forceUpdate()
    }

    /**
     * Checks if the device supports telephony subscriptions.
     * This ensures the device has SIM support and can manage cellular network subscriptions.
     */
    private fun hasTelephonySubscription(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_SUBSCRIPTION)
        } else {
            packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
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

    override fun forceUpdate() {
        val carrierInfo = if (hasTelephonySubscription()) {
            getCarrierName()
        } else {
            "unknown"
        }

        metadata.updateOrAddBy(CarrierInfo(carrierInfo = carrierInfo)) { it.name == metadataName }
    }
}
