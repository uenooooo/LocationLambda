package com.example.locationlambda.ui.model

import com.example.locationlambda.data.ActionType
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange

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
