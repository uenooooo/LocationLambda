package com.yasumo.locationlambda.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.ui.model.LocationRuleUi
import com.yasumo.locationlambda.ui.model.TransitionUi
import com.yasumo.locationlambda.ui.theme.EnterBlue
import com.yasumo.locationlambda.ui.theme.ExitOrange
import com.yasumo.locationlambda.ui.theme.LocationLambdaTheme
import com.yasumo.locationlambda.ui.theme.SandBackground

@Composable
fun LocationLambdaHomeScreen(
    rules: List<LocationRuleUi>,
    maxRules: Int,
    showDebugTools: Boolean,
    onOpenLog: () -> Unit,
    onEditRule: (LocationRuleUi) -> Unit,
    onEditEmptyRule: (Int) -> Unit,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit,
    onShowRequiredPermissions: () -> Unit,
    onDebugNotify: (LocationRuleUi) -> Unit
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
                if (showDebugTools) {
                    item {
                        HomeLogButton(onClick = onOpenLog)
                    }
                }
                item {
                    HomeHeader(
                        ruleCount = rules.count { it.hasRegisteredLocation() },
                        activeCount = rules.count { it.enabled },
                        maxRules = maxRules,
                        onShowRequiredPermissions = onShowRequiredPermissions
                    )
                }
                item {
                    RuleList(
                        rules = ruleSlots,
                        onEditRule = onEditRule,
                        onEditEmptyRule = onEditEmptyRule,
                        onToggleRule = onToggleRule,
                        showDebugTools = showDebugTools,
                        onDebugNotify = onDebugNotify
                    )
                }
            }
        }
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
                maxRules = 5,
                showDebugTools = true,
                onOpenLog = {},
                onEditRule = {},
                onEditEmptyRule = {},
                onToggleRule = { _, _ -> },
                onShowRequiredPermissions = {},
                onDebugNotify = {}
            )
        }
    }
}
