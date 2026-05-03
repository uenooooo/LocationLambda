package com.example.locationlambda.ui.home

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
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import com.example.locationlambda.ui.theme.SandBackground

@Composable
fun LocationLambdaHomeScreen(
    rules: List<LocationRuleUi>,
    maxRules: Int,
    showDebugTools: Boolean,
    onOpenLog: () -> Unit,
    onEditRule: (LocationRuleUi) -> Unit,
    onEditEmptyRule: (Int) -> Unit,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit,
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
                        maxRules = maxRules
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
            name = "\u6e0b\u8c37\u99c5",
            addressLabel = "\u6771\u4eac\u90fd\u6e0b\u8c37\u533a\u9053\u7384\u57421-1-1",
            areaLabel = "\u901a\u77e5\u534a\u5f84150m",
            transitions = listOf(TransitionUi("\u5230\u7740", EnterBlue)),
            actionTypeLabel = "URL\u3092\u958b\u304f",
            actionTargetLabel = "https://example.com",
            actionTargetValue = "https://example.com",
            enabled = true
        ),
        LocationRuleUi(
            id = "2",
            name = "\u4f1a\u793e",
            addressLabel = "\u6771\u4eac\u90fd\u5343\u4ee3\u7530\u533a\u4e38\u306e\u51851-1-1",
            areaLabel = "\u901a\u77e5\u534a\u5f84200m",
            transitions = listOf(TransitionUi("\u9000\u51fa", ExitOrange)),
            actionTypeLabel = "\u30a2\u30d7\u30ea\u3092\u958b\u304f",
            actionTargetLabel = "Teams",
            actionTargetValue = "com.microsoft.teams",
            enabled = true
        ),
        LocationRuleUi(
            id = "3",
            name = "\u30b8\u30e0",
            addressLabel = "\u6771\u4eac\u90fd\u65b0\u5bbf\u533a\u897f\u65b0\u5bbf2-2-2",
            areaLabel = "\u901a\u77e5\u534a\u5f84120m",
            transitions = listOf(
                TransitionUi("\u5230\u7740", EnterBlue),
                TransitionUi("\u9000\u51fa", ExitOrange)
            ),
            actionTypeLabel = "\u306a\u3057",
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
                onDebugNotify = {}
            )
        }
    }
}
