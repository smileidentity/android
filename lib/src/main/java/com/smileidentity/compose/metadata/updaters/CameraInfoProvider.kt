package com.smileidentity.compose.metadata.updaters

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.compose.metadata.models.MetadataKey
import com.smileidentity.compose.metadata.models.Metadatum

/**
 * A manager that updates metadata with camera information.
 */
internal class CameraInfoProvider(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?

    private fun getNumberOfCameras(): Any {
        return try {
            cameraManager?.cameraIdList?.size ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    override fun forceUpdate() {
        metadata.add(Metadatum(MetadataKey.NumberOfCameras, getNumberOfCameras()))
    }
}
