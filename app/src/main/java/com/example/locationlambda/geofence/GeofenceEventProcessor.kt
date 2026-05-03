package com.example.locationlambda.geofence

import android.content.Context
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.notification.GeofenceNotificationHelper
import com.example.locationlambda.storage.GeofenceStatusRepository
import com.example.locationlambda.storage.RuleRepository
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.model.toUi
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

internal class GeofenceEventProcessor(
    context: Context,
    private val repository: RuleRepository = RuleRepository(context),
    private val statusRepository: GeofenceStatusRepository = GeofenceStatusRepository(context),
    private val debugLogRepository: DebugLogRepository = DebugLogRepository(context),
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) {
    private val appContext = context.applicationContext

    fun process(event: GeofencingEvent) {
        if (event.hasError()) {
            logIgnored("\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9", "\u53d7\u4fe1\u30a8\u30e9\u30fc code=${event.errorCode}")
            statusRepository.markIgnored(
                "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
                "\u53d7\u4fe1\u30a8\u30e9\u30fc"
            )
            return
        }

        val transition = event.geofenceTransition
        if (!transition.isSupportedTransition()) {
            logIgnored("\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9", "\u5bfe\u8c61\u5916\u30a4\u30d9\u30f3\u30c8")
            statusRepository.markIgnored(
                "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
                "\u5bfe\u8c61\u5916\u30a4\u30d9\u30f3\u30c8"
            )
            return
        }

        val triggeredIds = event.triggeringGeofences
            ?.map { it.requestId }
            ?.toSet()
            .orEmpty()
        if (triggeredIds.isEmpty()) {
            logIgnored("\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9", "\u5bfe\u8c61\u306a\u3057")
            statusRepository.markIgnored(
                "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
                "\u5bfe\u8c61\u306a\u3057"
            )
            return
        }

        val rules = repository.loadRules()
        val now = nowMillis()
        var hasUpdates = false
        var hasMatchedRule = false

        val updatedRules = rules.map { rule ->
            if (!triggeredIds.contains(rule.id)) {
                return@map rule
            }
            hasMatchedRule = true
            debugLogRepository.append(
                type = DebugLogType.GEOFENCE,
                title = rule.name,
                detail = transition.toTransitionLabel()
            )
            if (!rule.enabled) {
                logIgnored(rule.name, "\u7121\u52b9")
                statusRepository.markIgnored(rule.name, "\u7121\u52b9")
                return@map rule
            }
            if (!rule.matchesTransition(transition)) {
                logIgnored(
                    rule.name,
                    "\u6761\u4ef6\u4e0d\u4e00\u81f4 \u53d7\u4fe1=${transition.toTransitionLabel()} \u8a2d\u5b9a=${rule.transitionType.toTransitionLabel()}"
                )
                statusRepository.markIgnored(rule.name, "\u6761\u4ef6\u4e0d\u4e00\u81f4")
                return@map rule
            }
            if (rule.isBoundaryJitter(transition, now)) {
                logIgnored(
                    rule.name,
                    "\u5883\u754c\u63fa\u308c \u524d\u56de=${rule.lastTriggeredTransition.toTransitionLabel()} \u4eca\u56de=${transition.toTransitionLabel()} ${rule.boundaryJitterElapsedSeconds(now)}s"
                )
                statusRepository.markIgnored(rule.name, "\u5883\u754c\u63fa\u308c")
                return@map rule
            }
            if (!rule.isCooldownReady(now)) {
                logIgnored(rule.name, "\u30af\u30fc\u30eb\u30c0\u30a6\u30f3\u4e2d remaining=${rule.cooldownRemainingSeconds(now)}s")
                statusRepository.markIgnored(rule.name, "\u30af\u30fc\u30eb\u30c0\u30a6\u30f3\u4e2d")
                return@map rule
            }

            hasUpdates = true
            rule.copy(
                lastTriggeredAt = now,
                lastTriggeredTransition = transition
            ).also { triggeredRule ->
                val triggeredRuleUi = triggeredRule.toUi().copy(
                    transitions = listOf(transition.toTransitionUi())
                )
                GeofenceNotificationHelper.showRuleNotification(appContext, triggeredRuleUi)
                statusRepository.markTriggered(rule.name, transition.toTransitionLabel())
            }
        }

        if (!hasMatchedRule) {
            logIgnored(
                "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9",
                "\u5bfe\u8c61\u30eb\u30fc\u30eb\u306a\u3057 ${triggeredIds.joinToString(",")}"
            )
        }

        if (hasUpdates) {
            repository.saveRules(updatedRules)
        }
    }

    private fun logIgnored(title: String, detail: String) {
        debugLogRepository.append(
            type = DebugLogType.IGNORED,
            title = title,
            detail = detail
        )
    }

    private fun Int.isSupportedTransition(): Boolean {
        return this == Geofence.GEOFENCE_TRANSITION_ENTER ||
            this == Geofence.GEOFENCE_TRANSITION_EXIT
    }

    private fun LocationRule.matchesTransition(googleTransition: Int): Boolean {
        return when (googleTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER ->
                LocationTransition.includesEnter(transitionType)
            Geofence.GEOFENCE_TRANSITION_EXIT ->
                LocationTransition.includesExit(transitionType)
            else -> false
        }
    }

    private fun LocationRule.isCooldownReady(now: Long): Boolean {
        val cooldownMillis = cooldownMin.coerceAtLeast(0) * 60_000L
        return cooldownMillis == 0L || now - lastTriggeredAt > cooldownMillis
    }

    private fun LocationRule.isBoundaryJitter(
        googleTransition: Int,
        now: Long
    ): Boolean {
        if (lastTriggeredAt <= 0L) return false
        if (!lastTriggeredTransition.isSupportedTransition()) return false
        if (!lastTriggeredTransition.isOppositeTransitionOf(googleTransition)) return false

        // 電車は5秒で100m前後進むことがある
        // 0.1秒間隔でinout通知が誤爆することがあるから5秒クールダウン
        return now - lastTriggeredAt <= BOUNDARY_JITTER_GUARD_MILLIS
    }

    private fun LocationRule.boundaryJitterElapsedSeconds(now: Long): Long {
        return ((now - lastTriggeredAt).coerceAtLeast(0L) + 999L) / 1_000L
    }

    private fun LocationRule.cooldownRemainingSeconds(now: Long): Long {
        val cooldownMillis = cooldownMin.coerceAtLeast(0) * 60_000L
        val remainingMillis = cooldownMillis - (now - lastTriggeredAt)
        return ((remainingMillis.coerceAtLeast(0L) + 999L) / 1_000L)
    }

    private fun Int.isOppositeTransitionOf(other: Int): Boolean {
        return this == Geofence.GEOFENCE_TRANSITION_ENTER &&
            other == Geofence.GEOFENCE_TRANSITION_EXIT ||
            this == Geofence.GEOFENCE_TRANSITION_EXIT &&
            other == Geofence.GEOFENCE_TRANSITION_ENTER
    }

    private fun Int.toTransitionUi(): TransitionUi {
        return when (this) {
            Geofence.GEOFENCE_TRANSITION_EXIT ->
                TransitionUi("\u9000\u51fa", ExitOrange)
            else ->
                TransitionUi("\u5230\u7740", EnterBlue)
        }
    }

    private fun Int.toTransitionLabel(): String {
        if (this == LocationTransition.BOTH) return "\u5230\u7740/\u9000\u51fa"
        return when (this) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> "\u9000\u51fa"
            else -> "\u5230\u7740"
        }
    }

    private companion object {
        private const val BOUNDARY_JITTER_GUARD_MILLIS = 5_000L
    }
}
