package com.smileidentity.metadata.updaters

import android.content.Context
import android.os.Build
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.Value
import com.smileidentity.metadata.updateOrAddBy

/**
 * A manager that updates metadata with build information.
 */
internal class BuildInfoMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.BuildInfo.key

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    init {
        forceUpdate()
    }

    private fun getBuildInfo(): Map<String, Value> {
        val buildSource = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11 (API 30) and above
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                // Below Android 11
                packageManager.getInstallerPackageName(packageName)
            } ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }

        return mapOf(
            "brand" to Value.StringValue(Build.BRAND),
            "device" to Value.StringValue(Build.DEVICE),
            "fingerprint" to Value.StringValue(Build.FINGERPRINT),
            "hardware" to Value.StringValue(Build.HARDWARE),
            "product" to Value.StringValue(Build.PRODUCT),
            "build_source" to Value.StringValue(buildSource),
        )
    }
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(Metadatum.BuildInfo(getBuildInfo())) { it.name == metadataName }
    }
}
