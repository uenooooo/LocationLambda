package com.yasumo.locationlambda.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.yasumo.locationlambda.debug.DebugDeviceStatusLogger
import com.yasumo.locationlambda.debug.DebugLogRepository
import com.yasumo.locationlambda.debug.DebugLogType
import com.yasumo.locationlambda.data.LocationRule
import com.yasumo.locationlambda.data.LocationTransition
import com.yasumo.locationlambda.data.hasRegisteredLocation
import com.yasumo.locationlambda.storage.GeofenceStatus
import com.yasumo.locationlambda.storage.GeofenceStatusRepository
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
            title = "再登録開始",
            detail = "rules=${rules.size} enabled=${rules.count { it.enabled }} max=$MAX_ACTIVE_GEOFENCES"
        )
        DebugDeviceStatusLogger.logPermissions(appContext, "再登録時")
        DebugDeviceStatusLogger.logStatus(appContext, "再登録時")

        val pendingIntent = buildPendingIntent()
        geofencingClient.removeGeofences(pendingIntent).addOnCompleteListener { removeTask ->
            debugLogRepository.append(
                type = DebugLogType.REGISTRATION,
                title = "既存登録解除",
                detail = if (removeTask.isSuccessful) {
                    "成功"
                } else {
                    "失敗 ${removeTask.exception?.message.orEmpty()}"
                }
            )
            if (!hasGeofencePermissions()) {
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "登録中止",
                    detail = "位置情報権限不足"
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
                    title = "登録なし",
                    detail = "有効なロケラムなし"
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
                    title = "全登録解除",
                    detail = if (task.isSuccessful) {
                        "成功"
                    } else {
                        "失敗 ${task.exception?.message.orEmpty()}"
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
            title = "初期トリガー",
            detail = "再登録直後は発火させない setInitialTrigger=0"
        )

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "登録成功",
                    detail = "count=${geofences.size}"
                )
                notifyStatus(statusRepository.markRegistrationSucceeded(geofences.size))
            }
            .addOnFailureListener { error ->
                debugLogRepository.append(
                    type = DebugLogType.REGISTRATION,
                    title = "登録失敗",
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
                detail = "登録対象外 location=${if (rule.hasRegisteredLocation()) "あり" else "なし"}"
            )
        }
        rules.filter { !it.enabled }.forEach { rule ->
            debugLogRepository.append(
                type = DebugLogType.REGISTRATION,
                title = rule.name.ifBlank { rule.id },
                detail = "登録対象外 無効"
            )
        }
    }

    private fun LocationRule.toRegistrationDetail(): String {
        return listOf(
            "登録対象",
            "radius=${radiusMeters.toInt()}m",
            "transition=${transitionType.toTransitionLabel()}",
            "lat=$latitude",
            "lon=$longitude"
        ).joinToString(" ")
    }

    private fun Int.toTransitionLabel(): String {
        val labels = mutableListOf<String>()
        if (LocationTransition.includesEnter(this)) labels += "到着"
        if (LocationTransition.includesExit(this)) labels += "退出"
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
        const val ACTION_GEOFENCE_EVENT = "com.yasumo.locationlambda.ACTION_GEOFENCE_EVENT"
        private const val MAX_ACTIVE_GEOFENCES = 5
    }
}
