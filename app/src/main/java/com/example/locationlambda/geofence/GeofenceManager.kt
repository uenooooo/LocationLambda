package com.example.locationlambda.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.LocationTransition
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

    fun reregister(rules: List<LocationRule>) {
        val pendingIntent = buildPendingIntent()
        geofencingClient.removeGeofences(pendingIntent).addOnCompleteListener {
            if (!hasGeofencePermissions()) {
                notifyStatus(statusRepository.markPermissionMissing())
                return@addOnCompleteListener
            }

            val geofences = rules
                .asSequence()
                .filter { it.enabled }
                .filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
                .take(MAX_ACTIVE_GEOFENCES)
                .mapNotNull { it.toGeofence() }
                .toList()

            if (geofences.isEmpty()) {
                notifyStatus(statusRepository.markNoRules())
                return@addOnCompleteListener
            }
            addGeofences(geofences, pendingIntent)
        }
    }

    fun clear() {
        geofencingClient.removeGeofences(buildPendingIntent())
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences(
        geofences: List<Geofence>,
        pendingIntent: PendingIntent
    ) {
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                notifyStatus(statusRepository.markRegistrationSucceeded(geofences.size))
            }
            .addOnFailureListener { error ->
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
