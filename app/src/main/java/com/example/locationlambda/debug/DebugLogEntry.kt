package com.example.locationlambda.debug

data class DebugLogEntry(
    val timestampMillis: Long,
    val type: DebugLogType,
    val title: String,
    val detail: String
)

enum class DebugLogType(val label: String) {
    NOTIFICATION("noti"),
    GEOFENCE("geof"),
    IGNORED("igno"),
    REGISTRATION("regi"),
    PERMISSION("perm"),
    RECEIVED("recv"),
    SUPPRESSED("supp"),
    RULE("rule"),
    STATUS("stat"),
    RESTORE("boot"),
    MARKER("mark")
}
