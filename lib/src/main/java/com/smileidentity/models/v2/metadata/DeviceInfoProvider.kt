package com.smileidentity.models.v2.metadata

import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics

class DeviceInfoProvider(context: Context) : MetadataProvider, SensorEventListener {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var currentOrientation: String = "unknown"
    private var deviceOrientations: MutableList<MetadataEntry> = mutableListOf()
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

    private fun getTotalMemoryInMB(): Any {
        activityManager?.let {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalMemoryInMB = memoryInfo.totalMem / (1024 * 1024)
            return totalMemoryInMB
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

    fun stopRecordingDeviceOrientations() {
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
        deviceOrientations.add(MetadataEntry(currentOrientation))
    }

    fun clearDeviceOrientations() {
        deviceOrientations.clear()
    }

    private fun getNumberOfCameras(): Any {
        return try {
            cameraManager?.cameraIdList?.size ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun hasProximitySensor(): Boolean {
        return sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
    }

    override fun collectMetadata(): Map<MetadataKey, MutableList<MetadataEntry>> {
        stopRecordingDeviceOrientations()

        val screenResolution = getScreenResolution()
        val totalMemory = getTotalMemoryInMB()
        val numberOfCameras = getNumberOfCameras()
        val hasProximitySensor = hasProximitySensor()

        return try {
            mapOf(
                MetadataKey.ScreenResolution to mutableListOf(MetadataEntry(screenResolution)),
                MetadataKey.MemoryInfo to mutableListOf(MetadataEntry(totalMemory)),
                MetadataKey.DeviceOrientation to deviceOrientations.toMutableList(),
                MetadataKey.NumberOfCameras to mutableListOf(MetadataEntry(numberOfCameras)),
                MetadataKey.ProximitySensor to mutableListOf(MetadataEntry(hasProximitySensor)),
            )
        } finally {
            // we clear the device orientations after we collected them
            deviceOrientations.clear()
        }
    }
}
