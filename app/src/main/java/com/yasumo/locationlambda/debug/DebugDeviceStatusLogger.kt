package com.yasumo.locationlambda.debug

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yasumo.locationlambda.BuildConfig
import com.yasumo.locationlambda.notification.GeofenceNotificationHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object DebugDeviceStatusLogger {
    fun logPermissions(context: Context, title: String) {
        if (!BuildConfig.SHOW_DEBUG_TOOLS) return

        DebugLogRepository(context).append(
            type = DebugLogType.PERMISSION,
            title = title,
            detail = context.permissionDetail()
        )
    }

    fun logStatus(context: Context, title: String) {
        if (!BuildConfig.SHOW_DEBUG_TOOLS) return

        DebugLogRepository(context).append(
            type = DebugLogType.STATUS,
            title = title,
            detail = context.statusDetail()
        )
    }

    private fun Context.permissionDetail(): String {
        val notification = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            "不要"
        } else {
            permissionLabel(Manifest.permission.POST_NOTIFICATIONS)
        }
        val backgroundLocation = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            "不要"
        } else {
            permissionLabel(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        return listOf(
            "通知=$notification",
            "fine=${permissionLabel(Manifest.permission.ACCESS_FINE_LOCATION)}",
            "coarse=${permissionLabel(Manifest.permission.ACCESS_COARSE_LOCATION)}",
            "background=$backgroundLocation"
        ).joinToString(" ")
    }

    private fun Context.statusDetail(): String {
        val googleAvailability = GoogleApiAvailability.getInstance()
        val googleStatus = googleAvailability.isGooglePlayServicesAvailable(this)
        val googleStatusLabel = if (googleStatus == ConnectionResult.SUCCESS) {
            "OK"
        } else {
            "NG($googleStatus:${googleAvailability.getErrorString(googleStatus)})"
        }

        return listOf(
            "位置情報=${locationEnabledLabel()}",
            "gps=${providerEnabledLabel(LocationManager.GPS_PROVIDER)}",
            "network=${providerEnabledLabel(LocationManager.NETWORK_PROVIDER)}",
            "gms=$googleStatusLabel",
            "通知=${notificationEnabledLabel()}",
            "channel=${notificationChannelLabel()}"
        ).joinToString(" ")
    }

    private fun Context.permissionLabel(permission: String): String {
        return if (
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            "許可"
        } else {
            "未許可"
        }
    }

    private fun Context.locationEnabledLabel(): String {
        val enabled = runCatching {
            val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return "不明"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                manager.isLocationEnabled
            } else {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        }.getOrNull() ?: return "不明"

        return enabled.toOnOffLabel()
    }

    private fun Context.providerEnabledLabel(provider: String): String {
        val enabled = runCatching {
            val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return "不明"
            manager.isProviderEnabled(provider)
        }.getOrNull() ?: return "不明"

        return enabled.toOnOffLabel()
    }

    private fun Context.notificationEnabledLabel(): String {
        return NotificationManagerCompat.from(this).areNotificationsEnabled().toOnOffLabel()
    }

    private fun Context.notificationChannelLabel(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "不要"

        val manager = getSystemService(NotificationManager::class.java) ?: return "不明"
        val channel = manager.getNotificationChannel(GeofenceNotificationHelper.CHANNEL_ID)
            ?: return "未作成"
        return (channel.importance != NotificationManager.IMPORTANCE_NONE).toOnOffLabel()
    }

    private fun Boolean.toOnOffLabel(): String {
        return if (this) "ON" else "OFF"
    }
}
