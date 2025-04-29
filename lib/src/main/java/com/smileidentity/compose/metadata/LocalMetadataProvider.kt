package com.smileidentity.compose.metadata

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Provides a local instance of `SnapshotStateList<Metadatum>` for composable functions within the composition.
 *
 * This object acts as a provider for `SnapshotStateList<Metadatum>` using `CompositionLocal`, allowing composable functions to access
 * the current `SnapshotStateList<Metadatum>` instance in a type-safe manner.
 *
 * The `current` property retrieves the `SnapshotStateList<Metadatum>` for the current composition.
 */
@SuppressLint("ComposeCompositionLocalUsage")
internal object LocalMetadataProvider {
    private val LocalMetadata = staticCompositionLocalOf<SnapshotStateList<Metadatum>> {
        error("LocalMetadataProvider was not set")
    }

    internal val current: SnapshotStateList<Metadatum>
        @Composable get() = if (LocalInspectionMode.current) {
            Metadata.Companion.default().items.toMutableStateList()
        } else {
            LocalMetadata.current
        }

    internal infix fun provides(
        metadata: SnapshotStateList<Metadatum>,
    ): ProvidedValue<SnapshotStateList<Metadatum>> {
        return LocalMetadata.provides(metadata)
    }
}
