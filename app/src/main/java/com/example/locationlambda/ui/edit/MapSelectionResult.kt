package com.example.locationlambda.ui.edit

data class MapSelectionResult(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val radiusMeters: Float,
    val radiusLabel: String
)
