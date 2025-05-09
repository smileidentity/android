package com.smileidentity.metadata.updaters

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.updateOrAddBy
import timber.log.Timber

/**
 * A manager that updates metadata with camera information.
 */
internal class CameraInfoMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.NumberOfCameras.key

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?

    init {
        forceUpdate()
    }

    private fun getNumberOfCameras(): Int {
        return try {
            cameraManager?.cameraIdList?.size ?: -1
        } catch (e: Exception) {
            Timber.e(e)
            -1
        }
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(Metadatum.NumberOfCameras(getNumberOfCameras())) {
            it.name == metadataName
        }
    }
}
