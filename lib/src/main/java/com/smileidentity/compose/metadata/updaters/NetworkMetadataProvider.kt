package com.smileidentity.compose.metadata.updaters

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.smileidentity.compose.metadata.models.MetadataKey
import com.smileidentity.models.v2.metadata.MetadataEntry
import com.smileidentity.compose.metadata.MetadataProvider
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI
import kotlin.collections.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class NetworkMetadataProvider(context: Context) : MetadataProvider {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var coroutineScope: CoroutineScope? = null
    private val connectionTypes = MutableStateFlow<List<MetadataEntry>>(listOf())

    fun startMonitoring() {
        // Check if the coroutine scope is already active to avoid starting multiple coroutines
        if (coroutineScope?.isActive == true) return

        Timber.d("Starting network connection monitoring")
        coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        coroutineScope?.launch {
            while (isActive) {
                val connection = getCurrentConnectionType()
                connection?.let { connectionType ->
                    val currentList = connectionTypes.value
                    if (currentList.lastOrNull()?.value != connectionType) {
                        Timber.d("Connection type changed to $connectionType. Adding to the list.")
                        connectionTypes.value = currentList + MetadataEntry(connectionType)
                    }
                }
                // We check the connection every second if there has been a change
                delay(1000)
            }
        }
    }

    fun stopMonitoring() {
        Timber.d("Stopping network connection monitoring")
        coroutineScope?.cancel()
    }

    @Suppress("Deprecation")
    private fun getCurrentConnectionType(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API 23+ (Marshmallow and above)
            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                else -> "other"
            }
        } else {
            // API 21 & 22 (Lollipop)
            val networkInfo = connectivityManager.activeNetworkInfo ?: return null
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> "wifi"
                ConnectivityManager.TYPE_MOBILE -> "cellular"
                else -> "other"
            }
        }
    }

    fun isProxyDetected(): Boolean {
        val proxyProperties =
            System.getProperty("http.proxyHost") ?: System.getProperty("https.proxyHost")
        if (!proxyProperties.isNullOrEmpty()) {
            return true
        }

        val proxyPort =
            System.getProperty("http.proxyPort") ?: System.getProperty("https.proxyPort")
        if (!proxyPort.isNullOrEmpty() && proxyPort.toIntOrNull() != 0) {
            return true
        }

        try {
            val defaultProxy = ProxySelector
                .getDefault()
                .select(URI("https://api.smileidentity.com/v1/ping"))
            if (defaultProxy != null) {
                for (proxy in defaultProxy) {
                    if (proxy.type() != Proxy.Type.DIRECT) {
                        val address = proxy.address()
                        if (address is InetSocketAddress) {
                            if (!address.hostName.isNullOrEmpty() && address.port != 0) {
                                return true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // do nothing, we're just checking for proxy
        }

        return false
    }

    @Suppress("Deprecation")
    fun isVPNActive(): Boolean {
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
        val isVpnUsingInterfaces = NetworkInterface.getNetworkInterfaces()?.toList()?.any {
                networkInterface ->
            vpnInterfacePrefixes.any { prefix -> networkInterface.name.startsWith(prefix) }
        } == true

        return isVpnUsingNetworkType || isVpnUsingInterfaces
    }

    override fun collectMetadata(): Map<MetadataKey, MutableList<MetadataEntry>> {
        val metadataEntries = connectionTypes.value
        val isVpnActive = isVPNActive()
        val isProxyDetected = isProxyDetected()
        return mapOf(
            MetadataKey.NetworkConnection to metadataEntries.toMutableList(),
            MetadataKey.VPNDetected to mutableListOf(MetadataEntry(isVpnActive)),
            MetadataKey.ProxyDetected to mutableListOf(MetadataEntry(isProxyDetected)),
        )
    }
}
