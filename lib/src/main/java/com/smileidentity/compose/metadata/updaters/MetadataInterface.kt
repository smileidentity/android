package com.smileidentity.compose.metadata.updaters

import androidx.lifecycle.DefaultLifecycleObserver

/**
 * Interface for metadata updaters that can automatically update metadata
 * based on device state or other factors.
 */
internal interface MetadataInterface : DefaultLifecycleObserver {
    /**
     * Force an immediate update of the metadata
     */
    fun forceUpdate()
}
