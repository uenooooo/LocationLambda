package com.yasumo.locationlambda.storage

import android.content.Context
import com.yasumo.locationlambda.data.ActionType
import com.yasumo.locationlambda.data.LocationRule
import com.yasumo.locationlambda.data.LocationTransition
import org.json.JSONArray
import org.json.JSONObject

class RuleRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "location_lambda_rules",
        Context.MODE_PRIVATE
    )

    fun loadRules(): List<LocationRule> {
        val savedJson = prefs.getString(KEY_RULES, null) ?: return defaultRules()
        return runCatching {
            val array = JSONArray(savedJson)
            buildList {
                for (index in 0 until array.length()) {
                    add(array.getJSONObject(index).toLocationRule())
                }
            }
        }.getOrElse { defaultRules() }
    }

    fun saveRules(rules: List<LocationRule>) {
        val array = JSONArray()
        rules.forEach { rule -> array.put(rule.toJson()) }
        prefs.edit().putString(KEY_RULES, array.toString()).apply()
    }

    private fun JSONObject.toLocationRule(): LocationRule {
        return LocationRule(
            id = getString("id"),
            name = optString("name", "-"),
            address = optString("address", "-"),
            latitude = optDouble("latitude", 0.0),
            longitude = optDouble("longitude", 0.0),
            radiusMeters = optDouble("radiusMeters", 100.0).toFloat(),
            transitionType = optInt("transitionType", LocationTransition.ENTER),
            actionType = runCatching {
                ActionType.valueOf(optString("actionType", ActionType.NOTIFICATION_ONLY.name))
            }.getOrDefault(ActionType.NOTIFICATION_ONLY),
            actionValue = optString("actionValue", ""),
            actionLabel = optString("actionLabel", ""),
            cooldownMin = optInt("cooldownMin", 0),
            enabled = optBoolean("enabled", true),
            lastTriggeredAt = optLong("lastTriggeredAt", 0L),
            lastTriggeredTransition = optInt("lastTriggeredTransition", 0)
        )
    }

    private fun LocationRule.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("name", name)
            .put("address", address)
            .put("latitude", latitude)
            .put("longitude", longitude)
            .put("radiusMeters", radiusMeters)
            .put("transitionType", transitionType)
            .put("actionType", actionType.name)
            .put("actionValue", actionValue)
            .put("actionLabel", actionLabel)
            .put("cooldownMin", cooldownMin)
            .put("enabled", enabled)
            .put("lastTriggeredAt", lastTriggeredAt)
            .put("lastTriggeredTransition", lastTriggeredTransition)
    }

    private fun defaultRules(): List<LocationRule> {
        return listOf(
            LocationRule(
                id = "1",
                name = "\u6e0b\u8c37\u99c5",
                address = "\u6771\u4eac\u90fd\u6e0b\u8c37\u533a\u9053\u7384\u57421-1-1",
                latitude = 35.658034,
                longitude = 139.701636,
                radiusMeters = 150f,
                transitionType = LocationTransition.ENTER,
                actionType = ActionType.URL,
                actionValue = "https://example.com",
                actionLabel = "https://example.com",
                cooldownMin = 0,
                enabled = true,
                lastTriggeredAt = 0L
            ),
            LocationRule(
                id = "2",
                name = "\u4f1a\u793e",
                address = "\u6771\u4eac\u90fd\u5343\u4ee3\u7530\u533a\u4e38\u306e\u51851-1-1",
                latitude = 35.681236,
                longitude = 139.767125,
                radiusMeters = 200f,
                transitionType = LocationTransition.EXIT,
                actionType = ActionType.APP,
                actionValue = "com.microsoft.teams",
                actionLabel = "Teams",
                cooldownMin = 0,
                enabled = true,
                lastTriggeredAt = 0L
            ),
            LocationRule(
                id = "3",
                name = "\u30b8\u30e0",
                address = "\u6771\u4eac\u90fd\u65b0\u5bbf\u533a\u897f\u65b0\u5bbf2-2-2",
                latitude = 35.689634,
                longitude = 139.692101,
                radiusMeters = 120f,
                transitionType = LocationTransition.BOTH,
                actionType = ActionType.NOTIFICATION_ONLY,
                actionValue = "",
                actionLabel = "",
                cooldownMin = 0,
                enabled = false,
                lastTriggeredAt = 0L
            )
        )
    }

    private companion object {
        const val KEY_RULES = "rules"
    }
}
