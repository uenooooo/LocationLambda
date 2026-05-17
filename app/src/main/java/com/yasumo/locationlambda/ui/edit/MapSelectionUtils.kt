package com.yasumo.locationlambda.ui.edit

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

internal val MapPlaceholderGray = Color(0xFF9AA6AD)

internal fun parseCoordinates(address: String): LatLng? {
    val parts = address.split(",").map { it.trim() }
    if (parts.size != 2) return null
    val latitude = parts[0].toDoubleOrNull() ?: return null
    val longitude = parts[1].toDoubleOrNull() ?: return null
    return LatLng(latitude, longitude)
}

internal fun hasRegisteredPosition(
    latitude: Double?,
    longitude: Double?,
    address: String
): Boolean {
    return latitude != null &&
        longitude != null &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        !(latitude == 0.0 && longitude == 0.0) &&
        address.isNotBlank() &&
        address != "-"
}

internal fun normalizeAddressLabel(address: String): String {
    return address.removePrefix("日本、").removePrefix("日本 ").trim()
}

internal fun normalizeRadiusLabel(radiusLabel: String): String {
    val meters = radiusLabel.filter { it.isDigit() }.toIntOrNull() ?: 100
    return "${meters}m"
}

internal fun String.toMetersFloat(): Float {
    return filter { it.isDigit() }.toFloatOrNull() ?: 100f
}
