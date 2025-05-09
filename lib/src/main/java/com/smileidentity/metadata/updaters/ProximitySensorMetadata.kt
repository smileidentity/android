package com.smileidentity.metadata.updaters

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.updateOrAddBy

/**
 * A manager that updates metadata with proximity sensor information.
 */
internal class ProximitySensorMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.ProximitySensor.key

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

    init {
        forceUpdate()
    }

    private fun hasProximitySensor(): Boolean {
        return sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(Metadatum.ProximitySensor(hasProximitySensor().toString())) {
            it.name == metadataName
        }
    }
}
