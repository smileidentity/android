package com.smileidentity.metadata.updaters

import android.content.Context
import android.content.res.Configuration
import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LifecycleOwner
import com.smileidentity.metadata.models.Metadatum

/**
 * A metadata updater that monitors device orientation and updates
 * the corresponding metadata entry.
 */
class DeviceOrientationMetadata(
    private val context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = "device_orientation"

    // Create orientation listener with medium sensor sensitivity
    private val orientationListener = object : OrientationEventListener(
        context,
        SensorManager.SENSOR_DELAY_NORMAL,
    ) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation != ORIENTATION_UNKNOWN) {
                val currentOrientation = getCurrentOrientation()
                updateOrientationMetadata(currentOrientation)
            }
        }
    }

    init {
        // Add initial orientation value to metadata
        updateOrientationMetadata(getCurrentOrientation())
    }

    override fun onStart(owner: LifecycleOwner) {
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()

            // Set initial orientation
            updateOrientationMetadata(getCurrentOrientation())
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        orientationListener.disable()
    }

    override fun forceUpdate() {
        updateOrientationMetadata(getCurrentOrientation())
    }

    /**
     * Update the device orientation metadata in the list
     */
    private fun updateOrientationMetadata(orientation: OrientationType) {
        val orientationMetadatum = when (orientation) {
            OrientationType.PORTRAIT -> Metadatum.DeviceOrientation.PORTRAIT
            OrientationType.LANDSCAPE -> Metadatum.DeviceOrientation.LANDSCAPE
            OrientationType.UNKNOWN -> Metadatum.DeviceOrientation.UNKNOWN
        }

        // Find and update existing metadata entry or add a new one
        val index = metadata.indexOfFirst { it.name == metadataName }
        if (index != -1) {
            metadata[index] = orientationMetadatum
        } else {
            metadata.add(orientationMetadatum)
        }
    }

    /**
     * Get the current device orientation
     */
    private fun getCurrentOrientation(): OrientationType {
        return when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> OrientationType.PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> OrientationType.LANDSCAPE
            else -> OrientationType.UNKNOWN
        }
    }

    enum class OrientationType {
        PORTRAIT,
        LANDSCAPE,
        UNKNOWN,
    }
}
