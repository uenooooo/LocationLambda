package com.yasumo.locationlambda.ui.model

import androidx.compose.ui.graphics.Color
import com.yasumo.locationlambda.data.ActionType
import com.yasumo.locationlambda.data.LocationTransition

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
