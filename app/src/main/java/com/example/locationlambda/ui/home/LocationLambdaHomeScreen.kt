package com.example.locationlambda.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.locationlambda.R
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.storage.GeofenceStatus
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Divider
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import com.example.locationlambda.ui.theme.SandBackground
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft
import com.example.locationlambda.ui.theme.SuccessGreen

@Composable
fun LocationLambdaHomeScreen(
    rules: List<LocationRuleUi>,
    geofenceStatus: GeofenceStatus,
    maxRules: Int,
    onEditRule: (LocationRuleUi) -> Unit,
    onEditEmptyRule: (Int) -> Unit,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit
) {
    val ruleSlots = rules.map<LocationRuleUi, LocationRuleUi?> { it } +
        List((maxRules - rules.size).coerceAtLeast(0)) { null }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 0.dp,
                    top = innerPadding.calculateTopPadding() + 20.dp,
                    end = 0.dp,
                    bottom = innerPadding.calculateBottomPadding() + 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    HomeHeader(
                        ruleCount = rules.count { it.hasRegisteredLocation() },
                        activeCount = rules.count { it.enabled },
                        maxRules = maxRules
                    )
                }
                item {
                    RuleList(
                        rules = ruleSlots,
                        onEditRule = onEditRule,
                        onEditEmptyRule = onEditEmptyRule,
                        onToggleRule = onToggleRule
                    )
                }
                item {
                    GeofenceStatusPanel(status = geofenceStatus)
                }
            }
        }
    }
}

private fun LocationRuleUi.hasRegisteredLocation(): Boolean {
    return latitude != null &&
        longitude != null &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        addressLabel.isNotBlank() &&
        addressLabel != "-"
}

@Composable
private fun GeofenceStatusPanel(status: GeofenceStatus) {
    Surface(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = Color(0xFFF3EEE5),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ジオフェンス状態",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Slate
            )
            StatusLine(
                label = "登録",
                value = "${status.registrationText} / ${status.registeredCount}件"
            )
            StatusLine(
                label = "最終登録",
                value = status.formattedLastRegisteredAt()
            )
            StatusLine(
                label = "最終発火",
                value = status.lastEventText
            )
            StatusLine(
                label = "発火時刻",
                value = status.formattedLastEventAt()
            )
        }
    }
}

@Composable
private fun StatusLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate
        )
    }
}

@Composable
private fun HomeHeader(ruleCount: Int, activeCount: Int, maxRules: Int) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.location_lambda_icon),
                contentDescription = "Location Lambda",
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "ロケーションラムダ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Slate
            )
        }
        Text(
            text = "場所に入る・出るときに通知（＋アクション）を設定できます。",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateSoft
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill(
                label = "設定 ${ruleCount}/${maxRules}件",
                color = Slate,
                textColor = CardSurface
            )
            StatPill(
                label = "有効${activeCount}件",
                color = SuccessGreen,
                textColor = CardSurface
            )
        }
    }
}

@Composable
private fun RuleList(
    rules: List<LocationRuleUi?>,
    onEditRule: (LocationRuleUi) -> Unit,
    onEditEmptyRule: (Int) -> Unit,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit
) {
    Surface(
        color = CardSurface,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column {
            rules.forEachIndexed { index, rule ->
                if (rule == null) {
                    EmptyRuleRow(
                        onClick = { onEditEmptyRule(index + 1) }
                    )
                } else {
                    RuleRow(
                        rule = rule,
                        onEditRule = onEditRule,
                        onToggleRule = onToggleRule
                    )
                }
                if (index != rules.lastIndex) {
                    HorizontalDivider(color = Divider)
                }
            }
        }
    }
}

@Composable
private fun EmptyRuleRow(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    text = "-",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateSoft
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Divider
                    )
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Divider
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = false,
                onCheckedChange = {},
                enabled = false,
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = CardSurface,
                    uncheckedTrackColor = Divider,
                    uncheckedBorderColor = Color.Transparent,
                    disabledUncheckedThumbColor = CardSurface,
                    disabledUncheckedTrackColor = Divider,
                    disabledUncheckedBorderColor = Color.Transparent
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "通知後アクション",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate
            )
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyLarge,
                color = Divider
            )
            Text(
                text = "対象",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate
            )
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyLarge,
                color = Divider
            )
        }
    }
}

@Composable
private fun RuleRow(
    rule: LocationRuleUi,
    onEditRule: (LocationRuleUi) -> Unit,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit
) {
    val context = LocalContext.current
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
                    onCheckedChange = { checked -> onToggleRule(rule, checked) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CardSurface,
                        checkedTrackColor = SuccessGreen,
                        uncheckedThumbColor = CardSurface,
                        uncheckedTrackColor = Divider,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "通知後アクション",
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
                text = "対象",
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
}

@Composable
private fun InlineInfo(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Slate
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateSoft
        )
    }
}

private fun String.toRadiusValueLabel(): String {
    return replace("\u901a\u77e5\u534a\u5f84", "").ifBlank { "-" }
}

private fun Int.toCooldownValueLabel(): String {
    if (this <= 0) return "\u306a\u3057"
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours == 0 -> "${minutes}\u5206"
        minutes == 0 -> "${hours}\u6642\u9593"
        else -> "${hours}\u6642\u9593${minutes}\u5206"
    }
}

@Composable
private fun StatPill(label: String, color: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
private fun TransitionBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationLambdaHomePreview() {
    val previewRules = listOf(
        LocationRuleUi(
            id = "1",
            name = "渋谷駅",
            addressLabel = "東京都渋谷区道玄坂1-1-1",
            areaLabel = "通知半径150m",
            transitions = listOf(TransitionUi("到着", EnterBlue)),
            actionTypeLabel = "URLを開く",
            actionTargetLabel = "https://example.com",
            actionTargetValue = "https://example.com",
            enabled = true
        ),
        LocationRuleUi(
            id = "2",
            name = "会社",
            addressLabel = "東京都千代田区丸の内1-1-1",
            areaLabel = "通知半径200m",
            transitions = listOf(TransitionUi("退出", ExitOrange)),
            actionTypeLabel = "アプリを開く",
            actionTargetLabel = "Teams",
            actionTargetValue = "com.microsoft.teams",
            enabled = true
        ),
        LocationRuleUi(
            id = "3",
            name = "ジム",
            addressLabel = "東京都新宿区西新宿2-2-2",
            areaLabel = "通知半径120m",
            transitions = listOf(
                TransitionUi("到着", EnterBlue),
                TransitionUi("退出", ExitOrange)
            ),
            actionTypeLabel = "なし",
            actionTargetLabel = "-",
            actionTargetValue = "",
            enabled = false
        )
    )

    LocationLambdaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SandBackground)
        ) {
            LocationLambdaHomeScreen(
                rules = previewRules,
                geofenceStatus = GeofenceStatus(
                    registrationText = "登録成功",
                    registeredCount = 2,
                    lastRegisteredAt = System.currentTimeMillis(),
                    lastEventText = "会社 を 退出",
                    lastEventAt = System.currentTimeMillis()
                ),
                maxRules = 5,
                onEditRule = {},
                onEditEmptyRule = {},
                onToggleRule = { _, _ -> }
            )
        }
    }
}
