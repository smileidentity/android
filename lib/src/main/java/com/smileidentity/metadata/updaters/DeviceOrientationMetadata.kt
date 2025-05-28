package com.smileidentity.metadata.updaters

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A metadata updater that monitors device orientation and updates the corresponding metadata entry.
 */
class DeviceOrientationMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.DeviceOrientation.key

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val sensorEventListener = object : SensorEventListener {
        var currentOrientation: OrientationType = OrientationType.UNKNOWN
        private var lastUpdateTime: Long = 0

        override fun onSensorChanged(event: SensorEvent?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 500) { // 0.5 seconds = 500 ms
                lastUpdateTime = currentTime
                detectOrientationChange(event)
                detectMovementChange(event)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No-op
        }

        private fun detectOrientationChange(event: SensorEvent?) {
            event?.let {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                currentOrientation = when {
                    abs(z) > 8.5 -> OrientationType.FLAT
                    abs(y) > abs(x) -> OrientationType.PORTRAIT
                    else -> OrientationType.LANDSCAPE
                }
            }
        }

        private fun detectMovementChange(event: SensorEvent?) {
            event?.let {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Calculate the acceleration magnitude
                val magnitude = sqrt(x * x + y * y + z * z)

                val gravity = SensorManager.GRAVITY_EARTH
                val movementChange = abs(magnitude - gravity)
                updateMovementMetadata(movementChange.toDouble())
            }
        }
    }

    companion object {
        lateinit var shared: DeviceOrientationMetadata
    }

    init {
        shared = this
    }

    override fun onStart(owner: LifecycleOwner) {
        accelerometer?.let {
            sensorManager?.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
            )
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    override fun forceUpdate() {
        updateOrientationMetadata(getCurrentOrientation())
    }

    private fun updateOrientationMetadata(orientation: OrientationType) {
        val orientationMetadatum = when (orientation) {
            OrientationType.PORTRAIT -> Metadatum.DeviceOrientation.PORTRAIT
            OrientationType.LANDSCAPE -> Metadatum.DeviceOrientation.LANDSCAPE
            OrientationType.FLAT -> Metadatum.DeviceOrientation.FLAT
            OrientationType.UNKNOWN -> Metadatum.DeviceOrientation.UNKNOWN
        }

        metadata.add(Metadatum.DeviceOrientation(orientationMetadatum.orientation))
    }

    private fun getCurrentOrientation(): OrientationType = sensorEventListener.currentOrientation

    private fun updateMovementMetadata(movementChange: Double) {
        metadata.add(Metadatum.DeviceMovement(movementChange))
    }

    enum class OrientationType {
        PORTRAIT,
        LANDSCAPE,
        FLAT,
        UNKNOWN,
    }
}
