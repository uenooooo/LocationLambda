package com.example.locationlambda.ui.edit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal suspend fun getCurrentPosition(context: Context): LatLng? {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasFineLocation && !hasCoarseLocation) return null

    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    return runCatching {
        suspendCancellableCoroutine { continuation ->
            @Suppress("MissingPermission")
            locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                continuation.resume(location?.let { LatLng(it.latitude, it.longitude) })
            }.addOnFailureListener {
                continuation.resume(null)
            }.addOnCanceledListener {
                continuation.resume(null)
            }
        }
    }.getOrNull()
}

internal suspend fun reverseGeocode(
    context: Context,
    position: LatLng
): String? {
    val geocoder = Geocoder(context, Locale.JAPAN)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(position.latitude, position.longitude, 1) { addresses ->
                continuation.resume(addresses.firstOrNull()?.toDisplayText())
            }
        }
    } else {
        withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(position.latitude, position.longitude, 1)
                    ?.firstOrNull()
                    ?.toDisplayText()
            }.getOrNull()
        }
    }
}

internal suspend fun geocodeLocationName(
    context: Context,
    query: String
): LatLng? {
    val geocoder = Geocoder(context, Locale.JAPAN)
    val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(query, 1) { addresses ->
                continuation.resume(addresses.firstOrNull())
            }
        }
    } else {
        withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 1)?.firstOrNull()
            }.getOrNull()
        }
    }

    return address?.let { LatLng(it.latitude, it.longitude) }
}

private fun Address.toDisplayText(): String {
    val lines = (0..maxAddressLineIndex)
        .mapNotNull { getAddressLine(it) }
        .filter { it.isNotBlank() }
    return lines.joinToString(" ")
}
