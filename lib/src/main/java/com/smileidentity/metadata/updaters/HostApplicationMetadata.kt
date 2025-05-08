package com.smileidentity.metadata.updaters

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.updateOrAddBy

/**
 * A manager that updates metadata with the host application information.
 */
internal class HostApplicationMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = "host_application"

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    init {
        forceUpdate()
    }

    private fun getHostApplication(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo?.let {
                val applicationInfo = packageInfo.applicationInfo
                applicationInfo?.let {
                    val appName = packageManager.getApplicationLabel(applicationInfo).toString()
                    val versionName = packageInfo.versionName
                    versionName?.let {
                        return "$appName v$versionName"
                    }
                    return appName
                }
            }
            "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(
            Metadatum.HostApplication(getHostApplication()),
        ) { it.name == metadataName }
    }
}
