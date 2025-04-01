package com.smileidentity.models.v2.metadata

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray

class NetworkMetadataProvider() : MetadataProvider {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val connectionTypes = MutableStateFlow<List<String>>(emptyList())

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        coroutineScope.launch {
            while (isActive) {
                val connection = getCurrentConnectionType()
                connection?.let { connection ->
                    val currentList = connectionTypes.value
                    if (currentList.lastOrNull() != connection) {
                        connectionTypes.value = currentList + connection
                    }
                }
                // we check the connection every second if there has been a change
                delay(1000)
            }
        }
    }

    fun stopMonitoring() {
        coroutineScope.cancel()
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

    override fun collectMetadata(): Map<MetadataKey, Any> {
        val jsonArray = JSONArray(connectionTypes.value)
        return mapOf(MetadataKey.NetworkConnection to jsonArray)
    }
}
