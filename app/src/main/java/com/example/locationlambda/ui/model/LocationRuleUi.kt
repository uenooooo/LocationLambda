package com.example.locationlambda.ui.model

import androidx.compose.ui.graphics.Color

data class LocationRuleUi(
    val id: String,
    val name: String,
    val addressLabel: String,
    val areaLabel: String,
    val transitions: List<TransitionUi>,
    val actionTypeLabel: String,
    val actionTargetLabel: String,
    val enabled: Boolean
)

data class TransitionUi(
    val label: String,
    val color: Color
)
