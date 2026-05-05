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
    return replace("\u901a\u77e5\u534a\u5f84", "").ifBlank { "-" }
}

internal fun Int.toCooldownValueLabel(): String {
    if (this <= 0) return "\u306a\u3057"
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours == 0 -> "${minutes}\u5206"
        minutes == 0 -> "${hours}\u6642\u9593"
        else -> "${hours}\u6642\u9593${minutes}\u5206"
    }
}
