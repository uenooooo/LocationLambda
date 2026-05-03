package com.example.locationlambda.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            DebugLogRepository(context).append(
                type = DebugLogType.RECEIVED,
                title = "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
                detail = "event=null action=${intent.action.orEmpty()}"
            )
            return
        }
        DebugLogRepository(context).append(
            type = DebugLogType.RECEIVED,
            title = "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
            detail = event.toReceiveDetail(intent.action)
        )
        GeofenceEventProcessor(context).process(event)
    }
}

private fun GeofencingEvent.toReceiveDetail(action: String?): String {
    val ids = triggeringGeofences
        ?.joinToString(",") { it.requestId }
        .orEmpty()
        .ifBlank { "-" }
    val error = if (hasError()) errorCode.toString() else "-"
    return "action=${action.orEmpty().ifBlank { "-" }} transition=${geofenceTransition.toTransitionLabel()} ids=$ids error=$error"
}

private fun Int.toTransitionLabel(): String {
    return when (this) {
        Geofence.GEOFENCE_TRANSITION_ENTER -> "\u5230\u7740"
        Geofence.GEOFENCE_TRANSITION_EXIT -> "\u9000\u51fa"
        else -> toString()
    }
}
