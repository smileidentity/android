package com.smileidentity.metadata.device

import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI

internal fun isProxyDetected(): Boolean {
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
