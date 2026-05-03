package com.example.locationlambda.debug

import android.content.Context
import com.example.locationlambda.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

class DebugLogRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "location_lambda_debug_logs",
        Context.MODE_PRIVATE
    )

    fun loadLogs(): List<DebugLogEntry> {
        val savedJson = prefs.getString(KEY_LOGS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(savedJson)
            buildList {
                for (index in 0 until array.length()) {
                    array.getJSONObject(index).toDebugLogEntry()?.let(::add)
                }
            }.sortedBy { it.timestampMillis }
        }.getOrElse { emptyList() }
    }

    fun append(type: DebugLogType, title: String, detail: String = "") {
        if (!BuildConfig.SHOW_DEBUG_TOOLS) return

        val logs = loadLogs() + DebugLogEntry(
            timestampMillis = System.currentTimeMillis(),
            type = type,
            title = title,
            detail = detail
        )
        saveLogs(logs.takeLast(MAX_LOG_COUNT))
    }

    fun appendMarker(label: String = "区切り") {
        append(
            type = DebugLogType.MARKER,
            title = "$LOG_MARKER_LINE $label $LOG_MARKER_LINE"
        )
    }

    private fun saveLogs(logs: List<DebugLogEntry>) {
        val array = JSONArray()
        logs.forEach { log -> array.put(log.toJson()) }
        prefs.edit().putString(KEY_LOGS, array.toString()).apply()
    }

    private fun JSONObject.toDebugLogEntry(): DebugLogEntry? {
        val typeName = optString("type", DebugLogType.NOTIFICATION.name)
        if (typeName == "ACTION") return null

        return DebugLogEntry(
            timestampMillis = optLong("timestampMillis", 0L),
            type = runCatching {
                DebugLogType.valueOf(typeName)
            }.getOrDefault(DebugLogType.NOTIFICATION),
            title = optString("title", ""),
            detail = optString("detail", "")
        )
    }

    private fun DebugLogEntry.toJson(): JSONObject {
        return JSONObject()
            .put("timestampMillis", timestampMillis)
            .put("type", type.name)
            .put("title", title)
            .put("detail", detail)
    }

    private companion object {
        const val KEY_LOGS = "logs"
        const val MAX_LOG_COUNT = 2_000
        const val LOG_MARKER_LINE = "------------------------------------------------------------"
    }
}
