package com.smileidentity.metadata.updaters

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.metadata.models.Metadatum

/**
 * A manager that updates metadata with network connection information.
 */
internal class NetworkMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = "network_connection"

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        private var connectionType: NetworkConnection? = null

        override fun onAvailable(network: android.net.Network) {
            val currentConnectionType = getConnectionType()
            if (currentConnectionType != connectionType) {
                updateNetworkMetadata(currentConnectionType)
                connectionType = currentConnectionType
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun forceUpdate() {
        updateNetworkMetadata(getConnectionType())
    }

    private fun updateNetworkMetadata(connection: NetworkConnection) {
        val connectionMetadatum = when (connection) {
            NetworkConnection.WIFI -> Metadatum.NetworkConnection.WIFI
            NetworkConnection.CELLULAR -> Metadatum.NetworkConnection.CELLULAR
            NetworkConnection.OTHER -> Metadatum.NetworkConnection.OTHER
            NetworkConnection.UNKNOWN -> Metadatum.NetworkConnection.UNKNOWN
        }

        metadata.add(Metadatum.NetworkConnection(connectionMetadatum.networkConnection))
    }

    private fun getConnectionType(): NetworkConnection {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API 23+ (Marshmallow and above)
            val network = connectivityManager.activeNetwork ?: return NetworkConnection.UNKNOWN
            val capabilities = connectivityManager.getNetworkCapabilities(network)
                ?: return NetworkConnection.UNKNOWN

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                    NetworkConnection.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                    NetworkConnection.CELLULAR
                else -> NetworkConnection.OTHER
            }
        } else {
            // API 21 & 22 (Lollipop)
            val networkInfo = connectivityManager.activeNetworkInfo
                ?: return NetworkConnection.UNKNOWN
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkConnection.WIFI
                ConnectivityManager.TYPE_MOBILE -> NetworkConnection.CELLULAR
                else -> NetworkConnection.OTHER
            }
        }
    }

    enum class NetworkConnection {
        WIFI,
        CELLULAR,
        OTHER,
        UNKNOWN,
    }
}
