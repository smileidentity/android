package com.smileidentity.compose.metadata.updaters

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.smileidentity.compose.metadata.models.MetadataKey
import com.smileidentity.compose.metadata.models.Metadatum

/**
 * A manager that updates metadata with screen resolution information.
 */
internal class MemoryProvider(
    context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

    private fun getTotalMemoryInMB(): Any {
        activityManager?.let {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalMemoryInMB = memoryInfo.totalMem / (1024 * 1024)
            return totalMemoryInMB
        }
        return "unknown"
    }

    override fun forceUpdate() {
        metadata.add(Metadatum(MetadataKey.MemoryInfo, getTotalMemoryInMB()))
    }
}
