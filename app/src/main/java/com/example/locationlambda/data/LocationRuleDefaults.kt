package com.example.locationlambda.data

internal fun LocationRule.hasRegisteredLocation(): Boolean {
    return latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        address.isNotBlank() &&
        address != "-"
}

internal fun createBlankRule(
    slotNumber: Int,
    existingRules: List<LocationRule>
): LocationRule {
    val id = (1..5)
        .map { it.toString() }
        .firstOrNull { candidate -> existingRules.none { it.id == candidate } }
        ?: "rule-$slotNumber"

    return LocationRule(
        id = id,
        name = "",
        address = "-",
        latitude = 0.0,
        longitude = 0.0,
        radiusMeters = 100f,
        transitionType = LocationTransition.ENTER,
        actionType = ActionType.NOTIFICATION_ONLY,
        actionValue = "",
        actionLabel = "",
        cooldownMin = 5,
        enabled = false,
        lastTriggeredAt = 0L
    )
}
