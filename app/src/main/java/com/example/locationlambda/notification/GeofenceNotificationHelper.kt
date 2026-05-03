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
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.ui.model.LocationRuleUi

object GeofenceNotificationHelper {
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

        val notificationId = rule.id.toNotificationId()
        val fallbackPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 10_000,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val launchIntent = RuleActionExecutor.buildLaunchIntent(context, rule)
        val actionPendingIntent = launchIntent?.let {
            buildActionPendingIntent(
                context = context,
                notificationId = notificationId,
                targetIntent = it
            )
        }

        val body = buildBody(rule)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.location_lambda_notification_icon)
            .setContentTitle(buildTitle(rule))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(actionPendingIntent ?: fallbackPendingIntent)

        if (body != null) {
            notificationBuilder
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

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
        val trigger = rule.transitions.firstOrNull()?.label ?: "\u5230\u7740"
        val particle = if (trigger == "\u9000\u51fa") "\u3092" else "\u306b"
        return "${rule.name} $particle $trigger"
    }

    private fun buildBody(rule: LocationRuleUi): String? {
        return when (rule.actionType) {
            ActionType.URL -> "URL\u3092\u958b\u304f\uff1a${rule.actionTargetValue}"
            ActionType.APP -> "\u30a2\u30d7\u30ea\u3092\u958b\u304f\uff1a${rule.actionTargetLabel}"
            ActionType.NOTIFICATION_ONLY -> null
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

    private fun String.toNotificationId(): Int {
        return hashCode() and Int.MAX_VALUE
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId,
            "\u30ed\u30b1\u30e9\u30e0\u901a\u77e5",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "\u5834\u6240\u306b\u5165\u308b\u30fb\u51fa\u308b\u3068\u304d\u306e\u901a\u77e5"
        }
        manager.createNotificationChannel(channel)
    }
}
