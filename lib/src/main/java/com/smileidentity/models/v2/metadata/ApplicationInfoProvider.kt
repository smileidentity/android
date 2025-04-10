package com.smileidentity.models.v2.metadata


import android.content.Context

class ApplicationInfoProvider(context: Context) : MetadataProvider {
    private val packageManager = context.packageManager
    private val packageName = context.packageName

    private fun getHostApplicationInfo(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo?.let{
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

    override fun collectMetadata(): Map<MetadataKey, Any> {
        val hostApplication = getHostApplicationInfo()
        return mapOf(
            MetadataKey.HostApplication to hostApplication,
        )
    }
}
