package com.example.locationlambda.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.locationlambda.debug.DebugDeviceStatusLogger
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.data.hasRegisteredLocation
import com.example.locationlambda.storage.GeofenceStatus
import com.example.locationlambda.storage.GeofenceStatusRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(
    context: Context,
    private val onStatusChanged: ((GeofenceStatus) -> Unit)? = null
) {
    private val appContext = context.applicationContext
    private val geofencingClient = LocationServices.getGeofencingClient(appContext)
    private val statusRepository = GeofenceStatusRepository(appContext)
    private val debugLogRepository = DebugLogRepository(appContext)

    fun reregister(rules: List<LocationRule>) {
        debugLogRepository.append(
            type = DebugLogType.REGISTRATION,
            title = "\u518d\u767b\u9332\u958b\u59cb",
            detail = "rules=${rules.size} enabled=${rules.count { it.enabled }} max=$MAX_ACTIVE_GEOFENCES"
        )
        DebugDeviceStatusLogger.logPermissions(appContext, "\u518d\u767b\u9332\u6642")
        DebugDeviceStatusLogger.logStatus(appContext, "\u518d\u767b\u9332\u6642")

        val pendingIntent = buildPendingIntent()
        geofencingClient.removeGeofences(pendingIntent).addOnCompleteListener { removeTask ->
            debugLogRepository.append(
                type = DebugLogType.REGISTRATION,
                title = "\u65e2\u5b58\u767b\u9332\u89e3\u9664",
                detail = if (removeTask.isSuccessful) {
                    "\u6210\u529f"
                } else {
                    "\u5931\u6557 ${removeTask.exception?.message.orEmpty()}"
                }
            )
            if (!hasGeofencePermissions()) {
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "\u767b\u9332\u4e2d\u6b62",
                    detail = "\u4f4d\u7f6e\u60c5\u5831\u6a29\u9650\u4e0d\u8db3"
                )
                notifyStatus(statusRepository.markPermissionMissing())
                return@addOnCompleteListener
            }

            val activeRules = rules
                .asSequence()
                .filter { it.enabled }
                .filter { it.hasRegisteredLocation() }
                .filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
                .take(MAX_ACTIVE_GEOFENCES)
                .toList()
            logSkippedRules(rules, activeRules)

            val geofences = activeRules.mapNotNull { it.toGeofence() }

            if (geofences.isEmpty()) {
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "\u767b\u9332\u306a\u3057",
                    detail = "\u6709\u52b9\u306a\u30ed\u30b1\u30e9\u30e0\u306a\u3057"
                )
                notifyStatus(statusRepository.markNoRules())
                return@addOnCompleteListener
            }
            activeRules.forEach { rule ->
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = rule.name.ifBlank { rule.id },
                    detail = rule.toRegistrationDetail()
                )
            }
            addGeofences(geofences, pendingIntent)
        }
    }

    fun clear() {
        geofencingClient.removeGeofences(buildPendingIntent())
            .addOnCompleteListener { task ->
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "\u5168\u767b\u9332\u89e3\u9664",
                    detail = if (task.isSuccessful) {
                        "\u6210\u529f"
                    } else {
                        "\u5931\u6557 ${task.exception?.message.orEmpty()}"
                    }
                )
            }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences(
        geofences: List<Geofence>,
        pendingIntent: PendingIntent
    ) {
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(geofences)
            .build()
        debugLogRepository.append(
            type = DebugLogType.SUPPRESSED,
            title = "\u521d\u671f\u30c8\u30ea\u30ac\u30fc",
            detail = "\u518d\u767b\u9332\u76f4\u5f8c\u306f\u767a\u706b\u3055\u305b\u306a\u3044 setInitialTrigger=0"
        )

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "\u767b\u9332\u6210\u529f",
                    detail = "count=${geofences.size}"
                )
                notifyStatus(statusRepository.markRegistrationSucceeded(geofences.size))
            }
            .addOnFailureListener { error ->
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "\u767b\u9332\u5931\u6557",
                    detail = error.message.orEmpty().ifBlank { error::class.java.simpleName }
                )
                notifyStatus(statusRepository.markRegistrationFailed(error.message))
            }
    }

    private fun LocationRule.toGeofence(): Geofence? {
        val transitionTypes = toGoogleTransitionTypes(transitionType)
        if (transitionTypes == 0) return null

        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, radiusMeters.coerceAtLeast(100f))
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionTypes)
            .build()
    }

    private fun toGoogleTransitionTypes(transitionType: Int): Int {
        var googleTransitionTypes = 0
        if (LocationTransition.includesEnter(transitionType)) {
            googleTransitionTypes = googleTransitionTypes or Geofence.GEOFENCE_TRANSITION_ENTER
        }
        if (LocationTransition.includesExit(transitionType)) {
            googleTransitionTypes = googleTransitionTypes or Geofence.GEOFENCE_TRANSITION_EXIT
        }
        return googleTransitionTypes
    }

    private fun logSkippedRules(
        rules: List<LocationRule>,
        activeRules: List<LocationRule>
    ) {
        val activeIds = activeRules.map { it.id }.toSet()
        rules.filter { it.enabled && it.id !in activeIds }.forEach { rule ->
            debugLogRepository.append(
                type = DebugLogType.REGISTRATION,
                title = rule.name.ifBlank { rule.id },
                detail = "\u767b\u9332\u5bfe\u8c61\u5916 location=${if (rule.hasRegisteredLocation()) "\u3042\u308a" else "\u306a\u3057"}"
            )
        }
        rules.filter { !it.enabled }.forEach { rule ->
            debugLogRepository.append(
                type = DebugLogType.REGISTRATION,
                title = rule.name.ifBlank { rule.id },
                detail = "\u767b\u9332\u5bfe\u8c61\u5916 \u7121\u52b9"
            )
        }
    }

    private fun LocationRule.toRegistrationDetail(): String {
        return listOf(
            "\u767b\u9332\u5bfe\u8c61",
            "radius=${radiusMeters.toInt()}m",
            "transition=${transitionType.toTransitionLabel()}",
            "lat=$latitude",
            "lon=$longitude"
        ).joinToString(" ")
    }

    private fun Int.toTransitionLabel(): String {
        val labels = mutableListOf<String>()
        if (LocationTransition.includesEnter(this)) labels += "\u5230\u7740"
        if (LocationTransition.includesExit(this)) labels += "\u9000\u51fa"
        return labels.joinToString("/").ifBlank { "-" }
    }

    private fun hasGeofencePermissions(): Boolean {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBackgroundLocation = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return hasFineLocation && hasBackgroundLocation
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(appContext, GeofenceReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun notifyStatus(status: GeofenceStatus) {
        onStatusChanged?.invoke(status)
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.example.locationlambda.ACTION_GEOFENCE_EVENT"
        private const val MAX_ACTIVE_GEOFENCES = 5
    }
}
