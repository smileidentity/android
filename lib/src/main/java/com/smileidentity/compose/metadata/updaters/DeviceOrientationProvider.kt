package com.smileidentity.compose.metadata.updaters

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.compose.metadata.models.Metadatum
import kotlin.math.abs

/**
 * A metadata updater that monitors device orientation and updates
 * the corresponding metadata entry.
 */
class DeviceOrientationProvider(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var currentOrientation: String = "unknown"
    private var deviceOrientations: MutableList<MetadataEntry> = mutableListOf()
    private var isRecordingDeviceOrientations = false

    override fun onStart(owner: LifecycleOwner) {
        accelerometer?.let {
            if (isRecordingDeviceOrientations) {
                // Early return if we are already recording the device orientations
                return
            }
            isRecordingDeviceOrientations = true

            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        isRecordingDeviceOrientations = false
        sensorManager?.unregisterListener(this)
    }

    override fun forceUpdate() {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]

            currentOrientation = when {
                abs(z) > 8.5 -> "Flat"
                abs(y) > abs(x) -> "Portrait"
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
}
