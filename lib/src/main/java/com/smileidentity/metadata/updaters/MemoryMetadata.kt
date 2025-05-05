package com.smileidentity.metadata.updaters

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.Metadatum.MemoryInfo
import com.smileidentity.metadata.updateOrAddBy

/**
 * A manager that updates metadata with device memory information.
 */
internal class MemoryMetadata(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = "memory_info"

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

    private fun getTotalMemoryInMB(): Long {
        activityManager?.let {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalMemoryInMB = memoryInfo.totalMem / (1024 * 1024)
            return totalMemoryInMB
        }
        return 0L
    }

    override fun forceUpdate() {
        metadata.updateOrAddBy(
            MemoryInfo(getTotalMemoryInMB().toString()),
        ) { it.name == metadataName }
    }
}
