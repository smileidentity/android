package com.smileidentity.compose.metadata.updaters

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.compose.metadata.models.MetadataKey
import com.smileidentity.compose.metadata.models.Metadatum

/**
 * A manager that updates metadata with proximity sensor information.
 */
internal class ProximitySensorProvider(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

    private fun hasProximitySensor(): Boolean {
        return sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
    }

    override fun forceUpdate() {
        metadata.add(Metadatum(MetadataKey.ProximitySensor, hasProximitySensor()))
    }
}
