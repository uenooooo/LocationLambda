package com.example.locationlambda.data

data class LocationRule(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val transitionType: Int,
    val actionType: ActionType,
    val actionValue: String,
    val actionLabel: String,
    val cooldownMin: Int,
    val enabled: Boolean,
    val lastTriggeredAt: Long
)
