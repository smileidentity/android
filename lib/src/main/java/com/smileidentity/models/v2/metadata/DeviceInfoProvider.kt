package com.smileidentity.models.v2.metadata

import android.app.ActivityManager
import android.content.Context
import android.hardware.camera2.CameraManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics
import org.json.JSONArray

class DeviceInfoProvider(context: Context) : MetadataProvider, SensorEventListener {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var currentOrientation: String = "unknown"
    private var deviceOrientations: MutableList<String> = mutableListOf()
    private var isRecordingDeviceOrientations = false

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

    fun startRecordingDeviceOrientations() {
        accelerometer?.let {
            if (isRecordingDeviceOrientations) {
                // Early return if we are already recording the device orientations
                return
            }
            isRecordingDeviceOrientations = true

            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopRecordingDeviceOrientations() {
        isRecordingDeviceOrientations = false
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            currentOrientation = when {
                kotlin.math.abs(z) > 8.5 -> "Flat"
                kotlin.math.abs(y) > kotlin.math.abs(x) -> "Portrait"
                else -> "Landscape"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun addDeviceOrientation() {
        deviceOrientations.add(currentOrientation)
    }

    fun clearDeviceOrientations() {
        deviceOrientations.clear()
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
        return sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
    }

    override fun collectMetadata(): Map<MetadataKey, Any> {
        stopRecordingDeviceOrientations()

        val screenResolution = getScreenResolution()
        val totalMemory = getTotalMemoryInMB()
        val numberOfCameras = getNumberOfCameras()
        val hasProximitySensor = hasProximitySensor()
        val jsonArray = JSONArray(deviceOrientations)

        // we clear the device orientations after we collected them
        deviceOrientations.clear()

        return mapOf(
            MetadataKey.ScreenResolution to screenResolution,
            MetadataKey.MemoryInfo to totalMemory,
            MetadataKey.DeviceOrientation to jsonArray,
            MetadataKey.NumberOfCameras to numberOfCameras,
            MetadataKey.ProximitySensor to hasProximitySensor,
        )
    }
}
