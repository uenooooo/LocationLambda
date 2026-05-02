package com.example.locationlambda.storage

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeofenceStatusRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "location_lambda_geofence_status",
        Context.MODE_PRIVATE
    )

    fun loadStatus(): GeofenceStatus {
        return GeofenceStatus(
            registrationText = prefs.getString(KEY_REGISTRATION_TEXT, "未登録") ?: "未登録",
            registeredCount = prefs.getInt(KEY_REGISTERED_COUNT, 0),
            lastRegisteredAt = prefs.getLong(KEY_LAST_REGISTERED_AT, 0L),
            lastEventText = prefs.getString(KEY_LAST_EVENT_TEXT, "なし") ?: "なし",
            lastEventAt = prefs.getLong(KEY_LAST_EVENT_AT, 0L)
        )
    }

    fun registerListener(
        onChange: (GeofenceStatus) -> Unit
    ): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            onChange(loadStatus())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return listener
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun markPermissionMissing(): GeofenceStatus {
        return updateRegistration(
            text = "権限不足",
            registeredCount = 0,
            timestamp = System.currentTimeMillis()
        )
    }

    fun markNoRules(): GeofenceStatus {
        return updateRegistration(
            text = "登録0件",
            registeredCount = 0,
            timestamp = System.currentTimeMillis()
        )
    }

    fun markRegistrationSucceeded(count: Int): GeofenceStatus {
        return updateRegistration(
            text = "登録成功",
            registeredCount = count,
            timestamp = System.currentTimeMillis()
        )
    }

    fun markRegistrationFailed(message: String?): GeofenceStatus {
        val detail = message?.takeIf { it.isNotBlank() } ?: "詳細なし"
        return updateRegistration(
            text = "登録失敗: $detail",
            registeredCount = 0,
            timestamp = System.currentTimeMillis()
        )
    }

    fun markTriggered(ruleName: String, transitionLabel: String): GeofenceStatus {
        return updateEvent(
            text = "$ruleName ${transitionLabel.toParticle()} $transitionLabel",
            timestamp = System.currentTimeMillis()
        )
    }

    fun markIgnored(ruleName: String, reason: String): GeofenceStatus {
        return updateEvent(
            text = "$ruleName を無視: $reason",
            timestamp = System.currentTimeMillis()
        )
    }

    private fun updateRegistration(
        text: String,
        registeredCount: Int,
        timestamp: Long
    ): GeofenceStatus {
        prefs.edit()
            .putString(KEY_REGISTRATION_TEXT, text)
            .putInt(KEY_REGISTERED_COUNT, registeredCount)
            .putLong(KEY_LAST_REGISTERED_AT, timestamp)
            .apply()
        return loadStatus()
    }

    private fun updateEvent(
        text: String,
        timestamp: Long
    ): GeofenceStatus {
        prefs.edit()
            .putString(KEY_LAST_EVENT_TEXT, text)
            .putLong(KEY_LAST_EVENT_AT, timestamp)
            .apply()
        return loadStatus()
    }

    private fun String.toParticle(): String {
        return if (this == "退出") "を" else "に"
    }

    private companion object {
        const val KEY_REGISTRATION_TEXT = "registration_text"
        const val KEY_REGISTERED_COUNT = "registered_count"
        const val KEY_LAST_REGISTERED_AT = "last_registered_at"
        const val KEY_LAST_EVENT_TEXT = "last_event_text"
        const val KEY_LAST_EVENT_AT = "last_event_at"
    }
}

data class GeofenceStatus(
    val registrationText: String,
    val registeredCount: Int,
    val lastRegisteredAt: Long,
    val lastEventText: String,
    val lastEventAt: Long
) {
    fun formattedLastRegisteredAt(): String {
        return lastRegisteredAt.toDisplayTime()
    }

    fun formattedLastEventAt(): String {
        return lastEventAt.toDisplayTime()
    }
}

private fun Long.toDisplayTime(): String {
    if (this <= 0L) return "-"
    return SimpleDateFormat("MM/dd HH:mm:ss", Locale.JAPAN).format(Date(this))
}
