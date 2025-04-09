package com.smileidentity.models.v2.metadata

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics

class DeviceInfoProvider(context: Context) : MetadataProvider {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?

    private fun getScreenResolution(): String {
        windowManager?.let {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+
                val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
                val width = windowMetrics.bounds.width()
                val height = windowMetrics.bounds.height()
                "$width x $height"
            } else {
                // API 29-
                val displayMetrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(displayMetrics)
                val width = displayMetrics.widthPixels
                val height = displayMetrics.heightPixels
                "$width x $height"
            }
        }
        return "unknown"
    }

    override fun collectMetadata(): Map<MetadataKey, Any> {
        val screenResolution = getScreenResolution()
        return mapOf(MetadataKey.ScreenResolution to screenResolution)
    }
}
