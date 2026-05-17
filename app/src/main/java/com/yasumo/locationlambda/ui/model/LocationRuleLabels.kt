package com.yasumo.locationlambda.ui.model

import com.yasumo.locationlambda.data.ActionType
import com.yasumo.locationlambda.data.LocationTransition
import com.yasumo.locationlambda.ui.theme.EnterBlue
import com.yasumo.locationlambda.ui.theme.ExitOrange

fun Float.toRadiusLabel(): String {
    val meters = toInt()
    return "通知半径${meters}m"
}

fun Int.toTransitionUi(): List<TransitionUi> {
    val transitions = mutableListOf<TransitionUi>()
    if (LocationTransition.includesEnter(this)) {
        transitions += TransitionUi("到着", EnterBlue)
    }
    if (LocationTransition.includesExit(this)) {
        transitions += TransitionUi("退出", ExitOrange)
    }
    if (transitions.isEmpty()) {
        transitions += TransitionUi("到着", EnterBlue)
    }
    return transitions
}

fun ActionType.toActionTypeLabel(): String {
    return when (this) {
        ActionType.URL -> "URLを開く"
        ActionType.APP -> "アプリを開く"
        ActionType.NOTIFICATION_ONLY -> "なし"
    }
}
