package com.example.locationlambda.ui.model

import com.example.locationlambda.data.ActionType
import com.example.locationlambda.data.LocationRule

fun LocationRule.toUi(): LocationRuleUi {
    return LocationRuleUi(
        id = id,
        name = name,
        addressLabel = address,
        areaLabel = radiusMeters.toRadiusLabel(),
        transitions = transitionType.toTransitionUi(),
        actionTypeLabel = actionType.toActionTypeLabel(),
        actionTargetLabel = when (actionType) {
            ActionType.URL -> actionValue.ifBlank { "-" }
            ActionType.APP -> actionLabel.ifBlank { "-" }
            ActionType.NOTIFICATION_ONLY -> "-"
        },
        actionTargetValue = actionValue,
        enabled = enabled,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        transitionType = transitionType,
        actionType = actionType,
        cooldownMin = cooldownMin,
        lastTriggeredAt = lastTriggeredAt
    )
}

fun LocationRuleUi.toDomain(previous: LocationRule? = null): LocationRule {
    val resolvedActionValue = when (actionType) {
        ActionType.URL -> actionTargetValue
        ActionType.APP -> actionTargetValue
        ActionType.NOTIFICATION_ONLY -> ""
    }
    val resolvedActionLabel = when (actionType) {
        ActionType.URL -> resolvedActionValue
        ActionType.APP -> actionTargetLabel
        ActionType.NOTIFICATION_ONLY -> ""
    }

    return LocationRule(
        id = id,
        name = name,
        address = addressLabel,
        latitude = latitude ?: previous?.latitude ?: 0.0,
        longitude = longitude ?: previous?.longitude ?: 0.0,
        radiusMeters = radiusMeters.takeIf { it > 0f } ?: previous?.radiusMeters ?: 100f,
        transitionType = transitionType,
        actionType = actionType,
        actionValue = resolvedActionValue,
        actionLabel = resolvedActionLabel,
        cooldownMin = cooldownMin,
        enabled = enabled,
        lastTriggeredAt = lastTriggeredAt
    )
}
