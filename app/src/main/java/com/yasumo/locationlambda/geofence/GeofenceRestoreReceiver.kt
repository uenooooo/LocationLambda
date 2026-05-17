package com.yasumo.locationlambda.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yasumo.locationlambda.debug.DebugLogRepository
import com.yasumo.locationlambda.debug.DebugLogType
import com.yasumo.locationlambda.storage.RuleRepository

class GeofenceRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.action.shouldRestoreGeofences()) return

        val appContext = context.applicationContext
        val rules = RuleRepository(appContext).loadRules()
        val debugLogs = DebugLogRepository(appContext)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            debugLogs.appendMarker(label = "端末再起動")
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
            Intent.ACTION_BOOT_COMPLETED -> "端末再起動"
            Intent.ACTION_MY_PACKAGE_REPLACED -> "アプリ更新"
            else -> "復旧"
        }
    }
}
