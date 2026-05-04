package com.example.locationlambda.geofence

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.example.locationlambda.BuildConfig
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            DebugLogRepository(appContext).append(
                type = DebugLogType.RECEIVED,
                title = "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
                detail = "event=null action=${intent.action.orEmpty()}"
            )
            return
        }
        DebugLogRepository(appContext).append(
            type = DebugLogType.RECEIVED,
            title = "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
            detail = event.toReceiveDetail(intent.action)
        )
        if (!BuildConfig.SHOW_DEBUG_TOOLS) {
            GeofenceEventProcessor(appContext).process(event)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                GeofenceLocationLogger(appContext).logCurrentLocation(event)
                GeofenceEventProcessor(appContext).process(event)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

private class GeofenceLocationLogger(context: Context) {
    private val appContext = context.applicationContext
    private val debugLogRepository = DebugLogRepository(appContext)
    private val locationClient = LocationServices.getFusedLocationProviderClient(appContext)

    suspend fun logCurrentLocation(event: GeofencingEvent) {
        val prefix = event.toLocationLogPrefix()
        if (!appContext.hasLocationPermission()) {
            debugLogRepository.append(
                type = DebugLogType.STATUS,
                title = "\u73fe\u5728\u5730",
                detail = "$prefix \u53d6\u5f97\u4e0d\u53ef reason=\u6a29\u9650\u4e0d\u8db3"
            )
            return
        }

        val location = withTimeoutOrNull(CURRENT_LOCATION_TIMEOUT_MILLIS) {
            appContext.getCurrentDebugLocation()
        }
        debugLogRepository.append(
            type = DebugLogType.STATUS,
            title = "\u73fe\u5728\u5730",
            detail = if (location == null) {
                "$prefix \u53d6\u5f97\u5931\u6557"
            } else {
                "$prefix ${location.toDebugLocationText()}"
            }
        )
    }

    private suspend fun Context.getCurrentDebugLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            @Suppress("MissingPermission")
            val task = locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            )
            task.addOnSuccessListener { location ->
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }.addOnFailureListener {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }.addOnCanceledListener {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }
    }
}

private fun GeofencingEvent.toReceiveDetail(action: String?): String {
    val ids = triggeringGeofences
        ?.joinToString(",") { it.requestId }
        .orEmpty()
        .ifBlank { "-" }
    val error = if (hasError()) errorCode.toString() else "-"
    val trigger = triggeringLocation?.toDebugLocationText(prefix = "trigger=")
        ?: "trigger=-"
    return "action=${action.orEmpty().ifBlank { "-" }} transition=${geofenceTransition.toTransitionLabel()} ids=$ids error=$error $trigger"
}

private fun GeofencingEvent.toLocationLogPrefix(): String {
    val ids = triggeringGeofences
        ?.joinToString(",") { it.requestId }
        .orEmpty()
        .ifBlank { "-" }
    return "transition=${geofenceTransition.toTransitionLabel()} ids=$ids"
}

private fun Context.hasLocationPermission(): Boolean {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return hasFineLocation || hasCoarseLocation
}

private fun Location.toDebugLocationText(prefix: String = ""): String {
    val accuracyText = if (hasAccuracy()) "${accuracy.toInt()}m" else "-"
    return "${prefix}lat=${latitude.formatCoordinate()} lon=${longitude.formatCoordinate()} accuracy=$accuracyText age=${ageSeconds()}s"
}

private fun Location.ageSeconds(): Long {
    val elapsedMillis = (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000L
    return ((elapsedMillis.coerceAtLeast(0L) + 999L) / 1_000L)
}

private fun Double.formatCoordinate(): String {
    return String.format(java.util.Locale.US, "%.7f", this)
}

private fun Int.toTransitionLabel(): String {
    return when (this) {
        Geofence.GEOFENCE_TRANSITION_ENTER -> "\u5230\u7740"
        Geofence.GEOFENCE_TRANSITION_EXIT -> "\u9000\u51fa"
        else -> toString()
    }
}

private const val CURRENT_LOCATION_TIMEOUT_MILLIS = 2_000L
