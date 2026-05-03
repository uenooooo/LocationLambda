package com.example.locationlambda.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.locationlambda.storage.RuleRepository

class GeofenceRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.action.shouldRestoreGeofences()) return

        val appContext = context.applicationContext
        GeofenceManager(appContext).reregister(RuleRepository(appContext).loadRules())
    }

    private fun String?.shouldRestoreGeofences(): Boolean {
        return this == Intent.ACTION_BOOT_COMPLETED ||
            this == Intent.ACTION_MY_PACKAGE_REPLACED
    }
}
