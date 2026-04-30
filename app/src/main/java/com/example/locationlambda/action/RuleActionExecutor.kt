package com.example.locationlambda.action

import android.content.Intent
import android.net.Uri
import com.example.locationlambda.ui.model.LocationRuleUi

object RuleActionExecutor {
    fun buildLaunchIntent(rule: LocationRuleUi): Intent? {
        return when (rule.actionTypeLabel) {
            "URLを開く" -> buildUrlIntent(rule.actionTargetValue)
            else -> null
        }
    }

    private fun buildUrlIntent(url: String): Intent? {
        val uri = runCatching { Uri.parse(url.trim()) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") return null

        return Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
