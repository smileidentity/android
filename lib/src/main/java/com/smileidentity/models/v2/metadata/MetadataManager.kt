package com.smileidentity.models.v2.metadata

import com.smileidentity.BuildConfig
import com.smileidentity.SmileID

interface MetadataProvider {
    fun collectMetadata(): Map<MetadataKey, Any>
}

object MetadataManager {
    private val providers: MutableList<MetadataProvider> = mutableListOf()
    private val staticMetadata: MutableMap<MetadataKey, String> = mutableMapOf()

    init {
        setDefaultMetadata()
        registerDefaultProviders()
    }

    private fun setDefaultMetadata() {
        addMetadata(MetadataKey.Sdk, "android")
        addMetadata(MetadataKey.SdkVersion, BuildConfig.VERSION_NAME)
        addMetadata(MetadataKey.ActiveLivenessVersion, "1.0.0")
        addMetadata(MetadataKey.ClientIP, getIPAddress(useIPv4 = true))
        addMetadata(MetadataKey.Fingerprint, SmileID.fingerprint)
        addMetadata(MetadataKey.DeviceModel, model)
        addMetadata(MetadataKey.DeviceOS, os)
    }

    private fun registerDefaultProviders() {
        //register(NetworkMetadataProvider())
    }

    fun launch() {
        /*
        This function is just here so we can start the collection of metadata from providers which
        is triggered by initialising the object.
        */
    }

    fun register(provider: MetadataProvider) {
        providers.add(provider)
    }

    fun addMetadata(key: MetadataKey, value: Any) {
        staticMetadata[key] = value.toString()
    }

    fun removeMetadata(key: MetadataKey) {
        staticMetadata.remove(key)
    }

    fun getDefaultMetadata(): Metadata {
        val metadata = staticMetadata.map { (key, value) ->
            Metadatum(key.key, value)
        }
        return Metadata(metadata)
    }

    fun collectAllMetadata(): List<Metadatum> {
        val allMetadata = mutableMapOf<MetadataKey, String>().apply {
            putAll(staticMetadata)
        }

        for (provider in providers) {
            val providerMetadata = provider.collectMetadata()
            for ((key, value) in providerMetadata) {
                allMetadata[key] = value.toString()
            }
        }

        return allMetadata.map { (key, value) ->
            Metadatum(key.key, value)
        }
    }
}
