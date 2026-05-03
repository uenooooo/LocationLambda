package com.example.locationlambda.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Divider
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft
import com.example.locationlambda.ui.theme.SuccessGreen

@Composable
internal fun RuleRow(
    rule: LocationRuleUi,
    onEditRule: (LocationRuleUi) -> Unit,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit
) {
    val context = LocalContext.current
    val canEnable = rule.hasRegisteredLocation()
    val appIcon = remember(rule.actionType, rule.actionTargetValue) {
        if (rule.actionType != ActionType.APP || rule.actionTargetValue.isBlank()) {
            null
        } else {
            runCatching {
                context.packageManager.getApplicationIcon(rule.actionTargetValue)
            }.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditRule(rule) }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RuleSummary(
            rule = rule,
            canEnable = canEnable,
            onToggleRule = onToggleRule
        )
        RuleActionSummary(rule = rule, appIcon = appIcon)
    }
}

@Composable
private fun RuleSummary(
    rule: LocationRuleUi,
    canEnable: Boolean,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = rule.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = rule.addressLabel,
                style = MaterialTheme.typography.bodySmall,
                color = SlateSoft
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rule.transitions.forEach { transition ->
                    TransitionBadge(
                        label = transition.label,
                        color = transition.color
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InlineInfo(
                    label = "\u534a\u5f84",
                    value = rule.areaLabel.toRadiusValueLabel()
                )
                InlineInfo(
                    label = "\u30af\u30fc\u30eb\u30c0\u30a6\u30f3",
                    value = rule.cooldownMin.toCooldownValueLabel()
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (rule.enabled) "ON" else "OFF",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (rule.enabled) SuccessGreen else SlateSoft
            )
            Switch(
                checked = rule.enabled,
                onCheckedChange = { checked ->
                    if (!checked || canEnable) {
                        onToggleRule(rule, checked)
                    }
                },
                enabled = rule.enabled || canEnable,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CardSurface,
                    checkedTrackColor = SuccessGreen,
                    uncheckedThumbColor = CardSurface,
                    uncheckedTrackColor = Divider,
                    uncheckedBorderColor = Color.Transparent,
                    disabledUncheckedThumbColor = CardSurface,
                    disabledUncheckedTrackColor = Divider,
                    disabledUncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun RuleActionSummary(
    rule: LocationRuleUi,
    appIcon: android.graphics.drawable.Drawable?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "\u901a\u77e5\u5f8c\u30a2\u30af\u30b7\u30e7\u30f3",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Slate
        )
        Text(
            text = rule.actionTypeLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = SlateSoft
        )
        Text(
            text = "\u5bfe\u8c61",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Slate
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBitmap = appIcon?.toBitmap()?.asImageBitmap()
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = rule.actionTargetLabel,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3EEE5))
                        .padding(2.dp)
                )
            }
            Text(
                text = rule.actionTargetLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = SlateSoft
            )
        }
    }
}
