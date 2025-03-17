package com.smileidentity.models.v2

import android.os.Parcelable
import com.smileidentity.models.v2.metadata.Metadatum
import kotlinx.parcelize.Parcelize

/**
 * Wrap Metadatum in a list. This allows for easy conversion with Moshi and the format the
 * backend expects
 */
@Parcelize
data class Metadata(val items: List<Metadatum>) : Parcelable {
    companion object {
        fun default(): Metadata = Metadata(
            listOf(
                Metadatum.Sdk,
                Metadatum.SdkVersion,
                Metadatum.ActiveLivenessVersion,
                Metadatum.ClientIP,
                Metadatum.Fingerprint,
                Metadatum.DeviceModel,
                Metadatum.DeviceOS,
            ),
        )
    }
}

fun List<Metadatum>.asNetworkRequest(): Metadata = Metadata(this)
