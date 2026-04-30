package com.example.locationlambda.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.locationlambda.MainActivity
import com.example.locationlambda.R
import com.example.locationlambda.action.RuleActionExecutor
import com.example.locationlambda.ui.model.LocationRuleUi

object MockNotificationHelper {
    private const val channelId = "geofence_channel"

    fun showRuleNotification(
        context: Context,
        rule: LocationRuleUi
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        createChannel(context)

        val notificationId = rule.id.hashCode()
        val fallbackPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 10_000,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val launchIntent = RuleActionExecutor.buildLaunchIntent(rule)
        val actionPendingIntent = launchIntent?.let {
            buildActionPendingIntent(
                context = context,
                notificationId = notificationId,
                targetIntent = it
            )
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(buildTitle(rule))
            .setContentText(buildBody(rule))
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildBody(rule)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(actionPendingIntent ?: fallbackPendingIntent)

        if (actionPendingIntent != null) {
            notificationBuilder.addAction(
                0,
                rule.actionTypeLabel,
                actionPendingIntent
            )
        }

        NotificationManagerCompat.from(context).notify(
            notificationId,
            notificationBuilder.build()
        )
        return true
    }

    private fun buildTitle(rule: LocationRuleUi): String {
        val trigger = rule.transitions.joinToString("・") { it.label }
        return "${rule.name} に $trigger"
    }

    private fun buildBody(rule: LocationRuleUi): String {
        return if (rule.actionTargetLabel == "-") {
            rule.actionTypeLabel
        } else {
            rule.actionTargetLabel
        }
    }

    private fun buildActionPendingIntent(
        context: Context,
        notificationId: Int,
        targetIntent: Intent
    ): PendingIntent {
        val proxyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_TARGET_INTENT, targetIntent)
        }

        return PendingIntent.getBroadcast(
            context,
            notificationId,
            proxyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId,
            "Geofence Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }
}
