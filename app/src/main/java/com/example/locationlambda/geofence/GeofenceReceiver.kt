package com.example.locationlambda.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.notification.MockNotificationHelper
import com.example.locationlambda.storage.GeofenceStatusRepository
import com.example.locationlambda.storage.RuleRepository
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.model.toUi
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        val statusRepository = GeofenceStatusRepository(context)
        if (event.hasError()) {
            statusRepository.markIgnored("ジオフェンス", "受信エラー")
            return
        }

        val transition = event.geofenceTransition
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            transition != Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            statusRepository.markIgnored("ジオフェンス", "対象外イベント")
            return
        }

        val triggeredIds = event.triggeringGeofences
            ?.map { it.requestId }
            ?.toSet()
            .orEmpty()
        if (triggeredIds.isEmpty()) {
            statusRepository.markIgnored("ジオフェンス", "対象なし")
            return
        }

        val repository = RuleRepository(context)
        val rules = repository.loadRules()
        val now = System.currentTimeMillis()
        var hasUpdates = false

        val updatedRules = rules.map { rule ->
            if (!triggeredIds.contains(rule.id)) {
                return@map rule
            }
            if (!rule.enabled) {
                statusRepository.markIgnored(rule.name, "無効")
                return@map rule
            }
            if (!rule.matchesTransition(transition)) {
                statusRepository.markIgnored(rule.name, "条件不一致")
                return@map rule
            }
            if (!rule.isCooldownReady(now)) {
                statusRepository.markIgnored(rule.name, "クールダウン中")
                return@map rule
            }

            val triggeredRule = rule.copy(lastTriggeredAt = now)
            val triggeredRuleUi = triggeredRule.toUi().copy(
                transitions = listOf(transition.toTransitionUi())
            )
            MockNotificationHelper.showRuleNotification(context, triggeredRuleUi)
            statusRepository.markTriggered(rule.name, transition.toTransitionLabel())
            hasUpdates = true
            triggeredRule
        }

        if (hasUpdates) {
            repository.saveRules(updatedRules)
        }
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

    private fun Int.toTransitionUi(): TransitionUi {
        return when (this) {
            Geofence.GEOFENCE_TRANSITION_EXIT ->
                TransitionUi("\u9000\u51fa", ExitOrange)
            else ->
                TransitionUi("\u5230\u7740", EnterBlue)
        }
    }

    private fun Int.toTransitionLabel(): String {
        return when (this) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> "\u9000\u51fa"
            else -> "\u5230\u7740"
        }
    }
}
