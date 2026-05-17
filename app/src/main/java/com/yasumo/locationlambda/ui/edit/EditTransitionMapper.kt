package com.yasumo.locationlambda.ui.edit

import com.yasumo.locationlambda.ui.model.TransitionUi
import com.yasumo.locationlambda.ui.theme.EnterBlue
import com.yasumo.locationlambda.ui.theme.ExitOrange

internal fun buildTransitions(onEnter: Boolean, onExit: Boolean): List<TransitionUi> {
    val transitions = mutableListOf<TransitionUi>()
    if (onEnter) transitions += TransitionUi("到着", EnterBlue)
    if (onExit) transitions += TransitionUi("退出", ExitOrange)
    if (transitions.isEmpty()) transitions += TransitionUi("到着", EnterBlue)
    return transitions
}
