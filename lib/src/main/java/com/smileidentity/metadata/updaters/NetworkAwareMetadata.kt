package com.smileidentity.metadata.updaters

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.metadata.models.Metadatum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A manager that updates metadata with network connectivity information.
 * This class observes network changes and updates the provided metadata list.
 */
internal class NetworkAwareMetadata(
    private val context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = "network_connectivity"

    private val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE,
    ) as ConnectivityManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val networkStatus = MutableStateFlow(NetworkStatus.UNKNOWN)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            networkStatus.value = NetworkStatus.AVAILABLE
        }

        override fun onLost(network: Network) {
            networkStatus.value = NetworkStatus.UNAVAILABLE
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            val status = when {
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    -> {
                    NetworkStatus.AVAILABLE
                }

                else -> NetworkStatus.UNAVAILABLE
            }
            networkStatus.value = status
        }
    }

    init {
        // Add initial network status to metadata
        updateNetworkMetadata(getCurrentNetworkStatus())

        // Collect network status changes
        coroutineScope.launch {
            networkStatus.collectLatest { status ->
                updateNetworkMetadata(status)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Set initial network status
        networkStatus.value = getCurrentNetworkStatus()
    }

    override fun onStop(owner: LifecycleOwner) {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Handle exception if needed
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        coroutineScope.cancel()
    }

    override fun forceUpdate() {
        updateNetworkMetadata(getCurrentNetworkStatus())
    }

    /**
     * Update the network connectivity metadata in the list
     */
    private fun updateNetworkMetadata(status: NetworkStatus) {
        val networkMetadatum = when (status) {
            NetworkStatus.AVAILABLE -> Metadatum.NetworkConnectivity.CONNECTED
            NetworkStatus.UNAVAILABLE -> Metadatum.NetworkConnectivity.DISCONNECTED
            NetworkStatus.UNKNOWN -> Metadatum.NetworkConnectivity.UNKNOWN
        }

        // Find and update existing metadata entry or add a new one
        val index = metadata.indexOfFirst { it.name == metadataName }
        if (index != -1) {
            metadata[index] = networkMetadatum
        } else {
            metadata.add(networkMetadatum)
        }
    }

    /**
     * Get the current network connection status
     */
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            when {
                capabilities == null -> NetworkStatus.UNAVAILABLE
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ->
                    NetworkStatus.AVAILABLE

                else -> NetworkStatus.UNAVAILABLE
            }
        } else {
            @Suppress("DEPRECATION")
            if (connectivityManager.activeNetworkInfo?.isConnected == true) {
                NetworkStatus.AVAILABLE
            } else {
                NetworkStatus.UNAVAILABLE
            }
        }
    }

    enum class NetworkStatus {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN,
    }
}
