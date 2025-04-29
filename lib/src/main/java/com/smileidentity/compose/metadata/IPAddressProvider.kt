package com.smileidentity.compose.metadata

import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale

internal fun getIPAddress(useIPv4: Boolean): String {
    return try {
        val networkInterfaces: List<NetworkInterface> = Collections.list(
            NetworkInterface.getNetworkInterfaces(),
        )
        for (networkInterface in networkInterfaces) {
            val addresses: List<InetAddress> = Collections.list(networkInterface.inetAddresses)
            for (item in addresses) {
                if (!item.isLoopbackAddress) {
                    val address = item.hostAddress
                    val isIPv4 = address.indexOf(':') < 0

                    if (useIPv4) {
                        if (isIPv4) {
                            return address
                        }
                    } else {
                        if (!isIPv4) {
                            val delim = address.indexOf('%') // drop ip6 zone suffix
                            return if (delim < 0) {
                                address.uppercase(Locale.getDefault())
                            } else {
                                address.substring(0, delim).uppercase(Locale.getDefault())
                            }
                        }
                    }
                }
            }
        }
        "" // Return empty string if no IP address is found
    } catch (ex: Exception) {
        "" // Return empty string on exception
    }
}
