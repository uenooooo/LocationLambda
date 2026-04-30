package com.example.locationlambda.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val targetIntent = intent.getParcelableExtraCompat<Intent>(EXTRA_TARGET_INTENT)

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        targetIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (targetIntent != null) {
            context.startActivity(targetIntent)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TARGET_INTENT = "extra_target_intent"
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
