package com.smileidentity.compose.metadata.models

import android.os.Parcelable
import com.smileidentity.BuildConfig
import com.smileidentity.SmileID
import com.smileidentity.compose.metadata.device.getIPAddress
import com.smileidentity.compose.metadata.device.locale
import com.smileidentity.compose.metadata.device.model
import com.smileidentity.compose.metadata.device.os
import com.smileidentity.compose.metadata.device.systemArchitecture
import com.smileidentity.compose.metadata.device.timezone
import com.smileidentity.util.getCurrentIsoTimestamp
import java.util.TimeZone
import kotlinx.parcelize.Parcelize

/**
 * Wrap Metadatum in a list. This allows for easy conversion with Moshi and the format the
 * backend expects
 */
@Parcelize
data class Metadata(val items: List<Metadatum>) : Parcelable {
    companion object {
        fun default(): Metadata = Metadata(
            listOfNotNull(
                Metadatum(MetadataKey.Sdk, "android"),
                Metadatum(MetadataKey.SdkVersion, BuildConfig.VERSION_NAME),
                Metadatum(MetadataKey.ActiveLivenessVersion, "1.0.0"),
                Metadatum(MetadataKey.ClientIP, getIPAddress(useIPv4 = true)),
                Metadatum(MetadataKey.Fingerprint, SmileID.fingerprint),
                Metadatum(MetadataKey.DeviceModel, model),
                Metadatum(MetadataKey.DeviceOS, os),
                Metadatum(MetadataKey.Timezone, timezone),
                Metadatum(MetadataKey.Locale, locale),
                Metadatum(MetadataKey.SystemArchitecture, systemArchitecture),
                Metadatum(
                    MetadataKey.LocalTimeOfEnrolment,
                    getCurrentIsoTimestamp(TimeZone.getDefault()),
                ),
                Metadatum(MetadataKey.SecurityPolicyVersion, "0.3.0"),
                Metadatum(MetadataKey.SdkLaunchCount, SmileID.sdkLaunchCount),
                SmileID.wrapperSdkName?.let { name ->
                    Metadatum(MetadataKey.WrapperName, name)
                },
                SmileID.wrapperSdkVersion?.let { version ->
                    Metadatum(MetadataKey.WrapperVersion, version)
                },
            ),
        )
    }
}
