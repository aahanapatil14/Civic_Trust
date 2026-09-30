package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class DamageGeoLocation(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val accuracyMeters: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isEstimatedOrFallback: Boolean = false
) {
    fun formatCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return String.format(Locale.US, "%.5f° %s, %.5f° %s", kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lonDir)
    }

    fun formatAccuracy(): String {
        return if (accuracyMeters > 0) "±${accuracyMeters.toInt()}m" else "Estimated"
    }
}

class DamageLocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(): DamageGeoLocation? {
        if (!hasLocationPermission()) {
            Log.w("DamageLocation", "Location permission not granted, cannot gather fresh GPS")
            return null
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(
                            DamageGeoLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitudeMeters = location.altitude,
                                accuracyMeters = location.accuracy,
                                timestamp = location.time,
                                isEstimatedOrFallback = false
                            )
                        )
                    } else {
                        // Fallback to last known location
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                continuation.resume(
                                    DamageGeoLocation(
                                        latitude = lastLoc.latitude,
                                        longitude = lastLoc.longitude,
                                        altitudeMeters = lastLoc.altitude,
                                        accuracyMeters = lastLoc.accuracy,
                                        timestamp = lastLoc.time,
                                        isEstimatedOrFallback = false
                                    )
                                )
                            } else {
                                continuation.resume(null)
                            }
                        }.addOnFailureListener {
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("DamageLocation", "Failed to retrieve location: ${e.message}", e)
                    continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        } catch (e: Exception) {
            Log.e("DamageLocation", "Error getting location", e)
            null
        }
    }
}
