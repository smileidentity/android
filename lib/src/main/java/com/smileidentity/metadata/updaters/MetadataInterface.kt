package com.smileidentity.metadata.updaters

import androidx.lifecycle.DefaultLifecycleObserver

/**
 * Interface for metadata updaters that can automatically update metadata
 * based on device state or other factors.
 */
interface MetadataInterface : DefaultLifecycleObserver {
    /**
     * The name of the metadata that this updater manages
     */
    val metadataName: String

    /**
     * Force an immediate update of the metadata
     */
    fun forceUpdate()
}
