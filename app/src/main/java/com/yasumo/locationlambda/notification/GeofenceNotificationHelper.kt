package com.yasumo.locationlambda.notification

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
import com.yasumo.locationlambda.MainActivity
import com.yasumo.locationlambda.R
import com.yasumo.locationlambda.action.RuleActionExecutor
import com.yasumo.locationlambda.data.ActionType
import com.yasumo.locationlambda.debug.DebugLogRepository
import com.yasumo.locationlambda.debug.DebugLogType
import com.yasumo.locationlambda.ui.model.LocationRuleUi

object GeofenceNotificationHelper {
    const val CHANNEL_ID = "geofence_channel"

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
            DebugLogRepository(context).append(
                type = DebugLogType.NOTIFICATION,
                title = "\u901a\u77e5\u5931\u6557",
                detail = "\u901a\u77e5\u6a29\u9650\u306a\u3057"
            )
            return false
        }

        createChannel(context)

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            DebugLogRepository(context).append(
                type = DebugLogType.NOTIFICATION,
                title = "\u901a\u77e5\u5931\u6557",
                detail = "\u30a2\u30d7\u30ea\u901a\u77e5OFF"
            )
            return false
        }
        if (!isChannelEnabled(context)) {
            DebugLogRepository(context).append(
                type = DebugLogType.NOTIFICATION,
                title = "\u901a\u77e5\u5931\u6557",
                detail = "\u901a\u77e5\u30c1\u30e3\u30f3\u30cd\u30ebOFF"
            )
            return false
        }

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
        if (launchIntent == null && rule.actionType != ActionType.NOTIFICATION_ONLY) {
            DebugLogRepository(context).append(
                type = DebugLogType.NOTIFICATION,
                title = "\u901a\u77e5\u5f8c\u30a2\u30af\u30b7\u30e7\u30f3\u6e96\u5099\u5931\u6557",
                detail = "${rule.actionTypeLabel} ${buildActionDetail(rule)}"
            )
        }
        val actionPendingIntent = launchIntent?.let {
            buildActionPendingIntent(
                context = context,
                notificationId = notificationId,
                targetIntent = it
            )
        }

        val title = buildTitle(rule)
        val body = buildBody(rule)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.location_lambda_notification_icon)
            .setContentTitle(title)
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

        val result = runCatching {
            notificationManager.notify(
                notificationId,
                notificationBuilder.build()
            )
        }
        if (result.isSuccess) {
            DebugLogRepository(context).append(
                type = DebugLogType.NOTIFICATION,
                title = title,
                detail = body.orEmpty()
            )
        } else {
            DebugLogRepository(context).append(
                type = DebugLogType.NOTIFICATION,
                title = "\u901a\u77e5\u5931\u6557",
                detail = result.exceptionOrNull()?.message.orEmpty()
            )
        }
        return result.isSuccess
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

    private fun buildActionDetail(rule: LocationRuleUi): String {
        return when (rule.actionType) {
            ActionType.URL -> rule.actionTargetValue
            ActionType.APP -> rule.actionTargetLabel
            ActionType.NOTIFICATION_ONLY -> ""
        }
    }

    private fun buildActionPendingIntent(
        context: Context,
        notificationId: Int,
        targetIntent: Intent
    ): PendingIntent {
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            notificationId,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun String.toNotificationId(): Int {
        return hashCode() and Int.MAX_VALUE
    }

    private fun isChannelEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true

        val manager = context.getSystemService(NotificationManager::class.java) ?: return true
        val channel = manager.getNotificationChannel(CHANNEL_ID) ?: return true
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ロケラム通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "\u5834\u6240\u306b\u5165\u308b\u30fb\u51fa\u308b\u3068\u304d\u306e\u901a\u77e5"
        }
        manager.createNotificationChannel(channel)
    }
}
