package com.smileidentity.metadata

import android.os.Parcelable
import com.smileidentity.metadata.models.Metadatum
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
                Metadatum.ActiveLivenessVersion,
                Metadatum.ClientIP,
                Metadatum.DeviceModel,
                Metadatum.DeviceOS,
                Metadatum.Fingerprint,
                Metadatum.Locale,
                Metadatum.LocalTimeOfEnrolment,
                Metadatum.SecurityPolicyVersion,
                Metadatum.Sdk,
                Metadatum.SdkVersion,
                Metadatum.SdkLaunchCount,
                Metadatum.SystemArchitecture,
                Metadatum.Timezone,
                Metadatum.WrapperSdkName,
                Metadatum.WrapperSdkVersion,
            ),
        )
    }
}

internal fun List<Metadatum>.asNetworkRequest(): Metadata = Metadata(this)

/**
 * Generic extension function to update or add an item in a MutableList based on a predicate
 */
fun <T> MutableList<T>.updateOrAddBy(item: T, predicate: (T) -> Boolean) {
    val index = indexOfFirst(predicate)
    if (index != -1) {
        this[index] = item
    } else {
        add(item)
    }
}
