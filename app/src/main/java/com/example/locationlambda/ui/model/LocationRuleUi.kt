package com.example.locationlambda.ui.model

import androidx.compose.ui.graphics.Color
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange

data class LocationRuleUi(
    val id: String,
    val name: String,
    val addressLabel: String,
    val areaLabel: String,
    val transitions: List<TransitionUi>,
    val actionTypeLabel: String,
    val actionTargetLabel: String,
    val actionTargetValue: String,
    val enabled: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float = 100f,
    val transitionType: Int = LocationTransition.ENTER,
    val actionType: ActionType = ActionType.NOTIFICATION_ONLY,
    val cooldownMin: Int = 0,
    val lastTriggeredAt: Long = 0L
)

data class TransitionUi(
    val label: String,
    val color: Color
)

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

fun Float.toRadiusLabel(): String {
    val meters = toInt()
    return "\u901a\u77e5\u534a\u5f84${meters}m"
}

fun Int.toTransitionUi(): List<TransitionUi> {
    val transitions = mutableListOf<TransitionUi>()
    if (LocationTransition.includesEnter(this)) {
        transitions += TransitionUi("\u5230\u7740", EnterBlue)
    }
    if (LocationTransition.includesExit(this)) {
        transitions += TransitionUi("\u9000\u51fa", ExitOrange)
    }
    if (transitions.isEmpty()) {
        transitions += TransitionUi("\u5230\u7740", EnterBlue)
    }
    return transitions
}

fun ActionType.toActionTypeLabel(): String {
    return when (this) {
        ActionType.URL -> "URL\u3092\u958b\u304f"
        ActionType.APP -> "\u30a2\u30d7\u30ea\u3092\u958b\u304f"
        ActionType.NOTIFICATION_ONLY -> "\u306a\u3057"
    }
}
