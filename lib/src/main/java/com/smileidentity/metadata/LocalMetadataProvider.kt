package com.smileidentity.metadata

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.updaters.CameraInfoMetadata
import com.smileidentity.metadata.updaters.CarrierInfoMetadata
import com.smileidentity.metadata.updaters.DeviceOrientationMetadata
import com.smileidentity.metadata.updaters.HostApplicationMetadata
import com.smileidentity.metadata.updaters.LocationMetadata
import com.smileidentity.metadata.updaters.MemoryMetadata
import com.smileidentity.metadata.updaters.MetadataInterface
import com.smileidentity.metadata.updaters.NetworkMetadata
import com.smileidentity.metadata.updaters.ProximitySensorMetadata
import com.smileidentity.metadata.updaters.ScreenResolutionMetadata
import com.smileidentity.metadata.updaters.VpnMetadata
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
    ): ProvidedValue<SnapshotStateList<Metadatum>> = LocalMetadata.provides(metadata)

    /**
     * Provides a fully extensible metadata provider that automatically updates
     * metadata based on various device states using pluggable updaters.
     *
     * @param providers List of factory functions for creating metadata updaters
     * @param content Composable content to provide the metadata to
     */
    @Composable
    fun MetadataProvider(
        providers: ImmutableList<(Context, SnapshotStateList<Metadatum>) -> MetadataInterface> =
            persistentListOf(
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    CameraInfoMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    CarrierInfoMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    DeviceOrientationMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    HostApplicationMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    MemoryMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    NetworkMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    ProximitySensorMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    ScreenResolutionMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    VpnMetadata(context, metadata)
                },
                { context: Context, metadata: SnapshotStateList<Metadatum> ->
                    LocationMetadata(context, metadata)
                },
            ),
        content: @Composable () -> Unit,
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // Create a mutable state list with default metadata
        val metadata = remember { Metadata.default().items.toMutableStateList() }

        // Create and remember all metadata updaters
        val updaters = remember(providers) {
            providers.map { factory -> factory(context, metadata) }
        }

        // Observe lifecycle for all updaters
        DisposableEffect(lifecycleOwner, updaters) {
            // Add all observers
            updaters.forEach { updater ->
                lifecycleOwner.lifecycle.addObserver(updater)
            }

            onDispose {
                // Remove all observers
                updaters.forEach { updater ->
                    lifecycleOwner.lifecycle.removeObserver(updater)
                }
            }
        }

        // Provide the enhanced metadata to the composition
        CompositionLocalProvider(LocalMetadata provides metadata) {
            content()
        }
    }
}
