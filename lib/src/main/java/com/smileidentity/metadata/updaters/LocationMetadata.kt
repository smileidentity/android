package com.smileidentity.metadata.updaters

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.smileidentity.metadata.models.MetadataKey
import com.smileidentity.metadata.models.Metadatum
import com.smileidentity.metadata.models.Value
import timber.log.Timber

/**
 * A manager that updates metadata with location information.
 */
internal class LocationMetadata(
    private val context: Context,
    private val metadata: SnapshotStateList<Metadatum>,
) : MetadataInterface {

    override val metadataName: String = MetadataKey.Geolocation.key

    private lateinit var fusedClient: FusedLocationProviderClient
    private val isFusedLocationAvailable: Boolean by lazy {
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(context)
        result == ConnectionResult.SUCCESS
    }

    private val locationListener = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            Timber.d("Location result received: ${result.lastLocation}")
            val location = result.lastLocation
                ?: return updateLocationMetadata(LocationInfo.UNKNOWN)
            val (accuracy, source) = classifyLocation(location)
            val info = LocationInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = accuracy,
                source = source,
            )
            Timber.d("Location received: $info")
            updateLocationMetadata(info)
            fusedClient.removeLocationUpdates(this)
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            if (!availability.isLocationAvailable) {
                Timber.d("Location not available, updating with UNKNOWN location")
                updateLocationMetadata(LocationInfo.UNKNOWN)
            }
            Timber.d("Location availability: ${availability.isLocationAvailable}")
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!isFusedLocationAvailable) {
            Timber.w("Fused Location Services not available, cannot update location metadata")
            updateLocationMetadata(LocationInfo.UNKNOWN)
            return
        }

        fusedClient = LocationServices.getFusedLocationProviderClient(context)
        forceUpdate()
    }

    override fun onStop(owner: LifecycleOwner) {
        fusedClient.removeLocationUpdates(locationListener)
    }

    override fun forceUpdate() {
        requestLocation()
    }

    private fun updateLocationMetadata(locationInfo: LocationInfo) {
        val info = mapOf(
            "latitude" to Value.DoubleValue(locationInfo.latitude),
            "longitude" to Value.DoubleValue(locationInfo.longitude),
            "accuracy" to Value.StringValue(locationInfo.accuracy.key),
            "source" to Value.StringValue(locationInfo.source.key),
        )
        metadata.add(Metadatum.GeoLocation(info))
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        Timber.d("Requesting location updates")
        if (!hasLocationPermission()) {
            Timber.d("Location permission not granted, cannot request location updates")
            updateLocationMetadata(LocationInfo.UNKNOWN)
            return
        }

        Timber.d("Location permission granted, proceeding with location request")
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L)
            .setWaitForAccurateLocation(false)
            .setMaxUpdates(1)
            .build()
        Timber.d("Location request created: $request")

        fusedClient.requestLocationUpdates(
            request,
            locationListener,
            null,
        )
    }

    private fun hasLocationPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private fun classifyLocation(location: Location): Pair<Accuracy, Source> {
        val accuracy = when {
            location.accuracy < 20 -> Accuracy.PRECISE
            location.accuracy < 100 -> Accuracy.APPROXIMATE
            else -> Accuracy.UNKNOWN
        }
        val source = when (location.provider) {
            LocationManager.GPS_PROVIDER -> Source.GPS
            LocationManager.NETWORK_PROVIDER -> Source.NETWORK
            LocationManager.FUSED_PROVIDER -> Source.FUSED
            else -> Source.UNKNOWN
        }
        return accuracy to source
    }

    data class LocationInfo(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Accuracy,
        val source: Source,
    ) {
        companion object {
            val UNKNOWN = LocationInfo(
                0.0,
                0.0,
                Accuracy.UNKNOWN,
                Source.UNKNOWN,
            )
        }
    }

    enum class Accuracy(val key: String) {
        PRECISE("precise"),
        APPROXIMATE("approximate"),
        UNKNOWN("unknown"),
    }

    enum class Source(val key: String) {
        GPS("gps"),
        NETWORK("network"),
        FUSED("fused"),
        UNKNOWN("unknown"),
    }
}
