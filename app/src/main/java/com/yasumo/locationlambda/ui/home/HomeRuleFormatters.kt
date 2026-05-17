package com.yasumo.locationlambda.ui.home

import com.yasumo.locationlambda.ui.model.LocationRuleUi

internal fun LocationRuleUi.hasRegisteredLocation(): Boolean {
    return latitude != null &&
        longitude != null &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        addressLabel.isNotBlank() &&
        addressLabel != "-"
}

internal fun String.toRadiusValueLabel(): String {
    return replace("通知半径", "").ifBlank { "-" }
}

internal fun Int.toCooldownValueLabel(): String {
    if (this <= 0) return "なし"
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours == 0 -> "${minutes}分"
        minutes == 0 -> "${hours}時間"
        else -> "${hours}時間${minutes}分"
    }
}
