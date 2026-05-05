package com.yasumo.locationlambda.ui.edit

import com.yasumo.locationlambda.ui.model.TransitionUi
import com.yasumo.locationlambda.ui.theme.EnterBlue
import com.yasumo.locationlambda.ui.theme.ExitOrange

internal fun buildTransitions(onEnter: Boolean, onExit: Boolean): List<TransitionUi> {
    val transitions = mutableListOf<TransitionUi>()
    if (onEnter) transitions += TransitionUi("\u5230\u7740", EnterBlue)
    if (onExit) transitions += TransitionUi("\u9000\u51fa", ExitOrange)
    if (transitions.isEmpty()) transitions += TransitionUi("\u5230\u7740", EnterBlue)
    return transitions
}
