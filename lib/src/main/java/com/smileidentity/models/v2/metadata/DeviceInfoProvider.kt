package com.smileidentity.models.v2.metadata

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import android.view.WindowMetrics
import org.json.JSONArray

class DeviceInfoProvider(private val context: Context) : MetadataProvider {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val configuration = context.resources.configuration
    private val orientations: MutableList<String> = mutableListOf()

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

    private fun getTotalMemoryInMB(): String {
        activityManager?.let {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalMemoryInMB = memoryInfo.totalMem / (1024 * 1024)
            return "$totalMemoryInMB"
        }
        return "unknown"
    }

    fun recordDeviceOrientation() {
        orientations.add(getDeviceOrientation())
    }

    private fun getDeviceOrientation(): String {
        val rotation: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+: use context.display
            context.display.rotation
        } else {
            // Below API 30: use defaultDisplay
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay?.rotation
        }
        val orientation = configuration.orientation

        return when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                "Portrait"
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                "Landscape"
            }
            else -> {
                // If orientation is unknown, default to rotation
                when (rotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> "Portrait"
                    Surface.ROTATION_90, Surface.ROTATION_270 -> "Landscape"
                    else -> "unknown"
                }
            }
        }
    }

    private fun getNumberOfCameras(): String {
        return try {
            val numberOfCameras = cameraManager?.cameraIdList?.size
            numberOfCameras.toString()
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun hasProximitySensor(): Boolean {
        val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximitySensor?.let {
            return true
        }
        return false
    }

    override fun collectMetadata(): Map<MetadataKey, Any> {
        val screenResolution = getScreenResolution()
        val totalMemory = getTotalMemoryInMB()
        val jsonArray = JSONArray(orientations)
        val numberOfCameras = getNumberOfCameras()
        val hasProximitySensor = hasProximitySensor()
        return mapOf(
            MetadataKey.ScreenResolution to screenResolution,
            MetadataKey.MemoryInfo to totalMemory,
            MetadataKey.DeviceOrientation to jsonArray,
            MetadataKey.NumberOfCameras to numberOfCameras,
            MetadataKey.ProximitySensor to hasProximitySensor,
        )
    }
}
