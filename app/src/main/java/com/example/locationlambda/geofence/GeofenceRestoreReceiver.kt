package com.example.locationlambda.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType
import com.example.locationlambda.storage.RuleRepository

class GeofenceRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.action.shouldRestoreGeofences()) return

        val appContext = context.applicationContext
        val rules = RuleRepository(appContext).loadRules()
        val debugLogs = DebugLogRepository(appContext)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            debugLogs.appendMarker(label = "\u7aef\u672b\u518d\u8d77\u52d5")
        }
        debugLogs.append(
            type = DebugLogType.RESTORE,
            title = intent.action.toRestoreTitle(),
            detail = "rules=${rules.size} enabled=${rules.count { it.enabled }}"
        )
        GeofenceManager(appContext).reregister(rules)
    }

    private fun String?.shouldRestoreGeofences(): Boolean {
        return this == Intent.ACTION_BOOT_COMPLETED ||
            this == Intent.ACTION_MY_PACKAGE_REPLACED
    }

    private fun String?.toRestoreTitle(): String {
        return when (this) {
            Intent.ACTION_BOOT_COMPLETED -> "\u7aef\u672b\u518d\u8d77\u52d5"
            Intent.ACTION_MY_PACKAGE_REPLACED -> "\u30a2\u30d7\u30ea\u66f4\u65b0"
            else -> "\u5fa9\u65e7"
        }
    }
}
