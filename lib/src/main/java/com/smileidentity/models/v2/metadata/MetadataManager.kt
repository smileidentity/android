package com.smileidentity.models.v2.metadata

import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.util.getCurrentIsoTimestamp
import java.util.TimeZone

interface MetadataProvider {
    fun collectMetadata(): Map<MetadataKey, Any>

    enum class MetadataProviderType {
        Network,
        CarrierInfo,
        DeviceInfo,
        ApplicationInfo,
    }
}

object MetadataManager {
    val providers: MutableMap<MetadataProvider.MetadataProviderType, MetadataProvider> =
        mutableMapOf()
    private val staticMetadata: MutableMap<MetadataKey, String> = mutableMapOf()

    init {
        setDefaultMetadata()
    }

    private fun setDefaultMetadata() {
        addMetadata(MetadataKey.Sdk, "android")
        addMetadata(MetadataKey.SdkVersion, BuildConfig.VERSION_NAME)
        addMetadata(MetadataKey.ActiveLivenessVersion, "1.0.0")
        addMetadata(MetadataKey.ClientIP, getIPAddress(useIPv4 = true))
        addMetadata(MetadataKey.Fingerprint, SmileID.fingerprint)
        addMetadata(MetadataKey.DeviceModel, model)
        addMetadata(MetadataKey.DeviceOS, os)
        addMetadata(MetadataKey.Timezone, timezone)
        addMetadata(MetadataKey.Locale, locale)
        addMetadata(MetadataKey.SystemArchitecture, systemArchitecture)
        addMetadata(MetadataKey.LocalTimeOfEnrolment, getCurrentIsoTimestamp(TimeZone.getDefault()))
        addMetadata(MetadataKey.SecurityPolicyVersion, "0.3.0")
    }

    fun register(type: MetadataProvider.MetadataProviderType, provider: MetadataProvider) {
        providers[type] = provider
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

        for ((_, provider) in providers) {
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
