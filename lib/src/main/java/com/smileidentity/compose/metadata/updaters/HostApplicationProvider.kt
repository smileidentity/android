package com.smileidentity.compose.metadata.updaters

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.compose.metadata.models.MetadataKey
import com.smileidentity.compose.metadata.models.Metadatum


/**
 * A manager that updates metadata with the host application information.
 */
internal class HostApplicationProvider(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    private val packageManager = context.packageManager
    private val packageName = context.packageName

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
        metadata.add(Metadatum(MetadataKey.HostApplication, getHostApplication()))
    }
}
