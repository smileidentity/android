package com.smileidentity.compose.metadata.updaters

import android.content.Context
import com.smileidentity.compose.metadata.models.MetadataKey
import com.smileidentity.models.v2.metadata.MetadataEntry
import com.smileidentity.compose.metadata.MetadataProvider

class ApplicationInfoProvider(context: Context) : MetadataProvider {
    private val packageManager = context.packageManager
    private val packageName = context.packageName

    private fun getHostApplicationInfo(): String {
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

    override fun collectMetadata(): Map<MetadataKey, MutableList<MetadataEntry>> {
        val hostApplication = getHostApplicationInfo()
        return mapOf(
            MetadataKey.HostApplication to mutableListOf(MetadataEntry(hostApplication)),
        )
    }
}
