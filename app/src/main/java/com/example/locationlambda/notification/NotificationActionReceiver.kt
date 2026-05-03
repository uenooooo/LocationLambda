package com.example.locationlambda.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val targetIntent = intent.getParcelableExtraCompat<Intent>(EXTRA_TARGET_INTENT)
        val logTitle = intent.getStringExtra(EXTRA_LOG_TITLE).orEmpty()
        val logDetail = intent.getStringExtra(EXTRA_LOG_DETAIL).orEmpty()

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
            context.getSystemService(NotificationManager::class.java)?.cancel(notificationId)
        }

        targetIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (targetIntent != null) {
            val result = runCatching {
                context.startActivity(targetIntent)
            }
            DebugLogRepository(context).append(
                type = DebugLogType.ACTION,
                title = if (result.isSuccess) {
                    logTitle.ifBlank { "\u30a2\u30af\u30b7\u30e7\u30f3\u5b9f\u884c" }
                } else {
                    "\u30a2\u30af\u30b7\u30e7\u30f3\u5931\u6557"
                },
                detail = if (result.isSuccess) {
                    logDetail
                } else {
                    result.exceptionOrNull()?.message.orEmpty()
                }
            )
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TARGET_INTENT = "extra_target_intent"
        const val EXTRA_LOG_TITLE = "extra_log_title"
        const val EXTRA_LOG_DETAIL = "extra_log_detail"
    }
}

private inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}
