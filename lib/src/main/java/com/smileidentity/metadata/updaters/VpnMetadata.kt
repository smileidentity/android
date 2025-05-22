package com.smileidentity.metadata.updaters

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.updateOrAddBy
import java.net.NetworkInterface

/**
 * A manager that updates metadata with VPN information.
 */
internal class VpnMetadata(context: Context, private val metadata: SnapshotStateList<Metadatum>) :
    MetadataInterface {

    override val metadataName: String = MetadataKey.VPNDetected.key

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        forceUpdate()
    }

    private fun isVPNActive(): Boolean {
        val isVpnUsingNetworkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API 23+ (Marshmallow and above)
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        } else {
            // For API 21-22 (Lollipop)
            connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_VPN
        }

        // Check for VPN interfaces (Works across all API levels)
        val vpnInterfacePrefixes = listOf("tun", "tap", "ppp", "ipsec", "utun")
        val isVpnUsingInterfaces =
            NetworkInterface.getNetworkInterfaces()?.toList()?.any { networkInterface ->
                vpnInterfacePrefixes.any { prefix -> networkInterface.name.startsWith(prefix) }
            } == true

        return isVpnUsingNetworkType || isVpnUsingInterfaces
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(Metadatum.Vpn(isVPNActive())) { it.name == metadataName }
    }
}
