package com.example.locationlambda.debug

data class DebugLogEntry(
    val timestampMillis: Long,
    val type: DebugLogType,
    val title: String,
    val detail: String
)

enum class DebugLogType(val label: String) {
    NOTIFICATION("noti"),
    ACTION("acti"),
    GEOFENCE("geof"),
    IGNORED("igno")
}
