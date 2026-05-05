package com.yasumo.locationlambda.data

object LocationTransition {
    const val ENTER = 1
    const val EXIT = 2
    const val BOTH = ENTER or EXIT

    fun fromFlags(onEnter: Boolean, onExit: Boolean): Int {
        return when {
            onEnter && onExit -> BOTH
            onExit -> EXIT
            else -> ENTER
        }
    }

    fun includesEnter(transitionType: Int): Boolean {
        return transitionType and ENTER == ENTER
    }

    fun includesExit(transitionType: Int): Boolean {
        return transitionType and EXIT == EXIT
    }
}
