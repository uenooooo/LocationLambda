package com.example.locationlambda.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
fun LocationLambdaHomeScreen() {
    val maxRules = 5
    val sampleRules = listOf(
        LocationRuleUi(
            name = "渋谷駅",
            addressLabel = "東京都渋谷区道玄坂1-1-1",
            areaLabel = "半径150m",
            transitions = listOf(
                TransitionUi("到着", EnterBlue)
            ),
            actionTypeLabel = "URLを開く",
            actionTargetLabel = "https://example.com",
            enabled = true
        ),
        LocationRuleUi(
            name = "会社",
            addressLabel = "東京都千代田区丸の内1-1-1",
            areaLabel = "半径200m",
            transitions = listOf(
                TransitionUi("退出", ExitOrange)
            ),
            actionTypeLabel = "アプリを開く",
            actionTargetLabel = "Teams",
            enabled = true
        ),
        LocationRuleUi(
            name = "ジム",
            addressLabel = "東京都新宿区西新宿2-2-2",
            areaLabel = "半径120m",
            transitions = listOf(
                TransitionUi("到着", EnterBlue),
                TransitionUi("退出", ExitOrange)
            ),
            actionTypeLabel = "なし",
            actionTargetLabel = "-",
            enabled = false
        )
    )
    val ruleSlots = sampleRules.map<LocationRuleUi, LocationRuleUi?> { it } +
        List((maxRules - sampleRules.size).coerceAtLeast(0)) { null }

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
                        ruleCount = sampleRules.size,
                        activeCount = sampleRules.count { it.enabled },
                        maxRules = maxRules
                    )
                }
                item {
                    RuleList(rules = ruleSlots)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(ruleCount: Int, activeCount: Int, maxRules: Int) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ロケラム一覧",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Slate
        )
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
private fun RuleList(rules: List<LocationRuleUi?>) {
    Surface(
        color = CardSurface,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column {
            rules.forEachIndexed { index, rule ->
                if (rule == null) {
                    EmptyRuleRow()
                } else {
                    RuleRow(rule = rule)
                }
                if (index != rules.lastIndex) {
                    HorizontalDivider(color = Divider)
                }
            }
        }
    }
}

@Composable
private fun EmptyRuleRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    disabledUncheckedThumbColor = CardSurface,
                    disabledUncheckedTrackColor = Divider
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "実行アクション",
                style = MaterialTheme.typography.labelMedium,
                color = SlateSoft
            )
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyLarge,
                color = Divider
            )
            Text(
                text = "対象",
                style = MaterialTheme.typography.labelMedium,
                color = SlateSoft
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
private fun RuleRow(rule: LocationRuleUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                    Text(
                        text = rule.areaLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = rule.enabled,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CardSurface,
                    checkedTrackColor = SuccessGreen,
                    uncheckedThumbColor = CardSurface,
                    uncheckedTrackColor = Divider
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "実行アクション",
                style = MaterialTheme.typography.labelMedium,
                color = SlateSoft
            )
            Text(
                text = rule.actionTypeLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = Slate
            )
            Text(
                text = "対象",
                style = MaterialTheme.typography.labelMedium,
                color = SlateSoft
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rule.actionTargetLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate
                )
                Spacer(modifier = Modifier.weight(1f))
                EditButton()
            }
        }
    }
}

@Composable
private fun EditButton() {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFF3EEE5))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "編集",
            style = MaterialTheme.typography.labelLarge,
            color = Slate
        )
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

private data class LocationRuleUi(
    val name: String,
    val addressLabel: String,
    val areaLabel: String,
    val transitions: List<TransitionUi>,
    val actionTypeLabel: String,
    val actionTargetLabel: String,
    val enabled: Boolean
)

private data class TransitionUi(
    val label: String,
    val color: Color
)

@Preview(showBackground = true)
@Composable
private fun LocationLambdaHomePreview() {
    LocationLambdaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SandBackground)
        ) {
            LocationLambdaHomeScreen()
        }
    }
}
