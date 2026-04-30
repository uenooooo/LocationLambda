package com.example.locationlambda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.locationlambda.ui.edit.LocationLambdaEditScreen
import com.example.locationlambda.ui.home.LocationLambdaHomeScreen
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.splash.LocationLambdaSplashScreen
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationLambdaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LocationLambdaApp()
                }
            }
        }
    }
}

@Composable
private fun LocationLambdaApp() {
    var showSplash by remember { mutableStateOf(true) }
    val maxRules = 5
    var rules by remember {
        mutableStateOf(
            listOf(
                LocationRuleUi(
                    id = "1",
                    name = "渋谷駅",
                    addressLabel = "東京都渋谷区道玄坂1-1-1",
                    areaLabel = "半径150m",
                    transitions = listOf(TransitionUi("到着", EnterBlue)),
                    actionTypeLabel = "URLを開く",
                    actionTargetLabel = "https://example.com",
                    enabled = true
                ),
                LocationRuleUi(
                    id = "2",
                    name = "会社",
                    addressLabel = "東京都千代田区丸の内1-1-1",
                    areaLabel = "半径200m",
                    transitions = listOf(TransitionUi("退出", ExitOrange)),
                    actionTypeLabel = "アプリを開く",
                    actionTargetLabel = "Teams",
                    enabled = true
                ),
                LocationRuleUi(
                    id = "3",
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
        )
    }
    var editingRuleId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(1_500)
        showSplash = false
    }

    if (showSplash) {
        LocationLambdaSplashScreen()
    } else {
        val editingRule = rules.firstOrNull { it.id == editingRuleId }
        if (editingRule != null) {
            LocationLambdaEditScreen(
                rule = editingRule,
                onBack = { editingRuleId = null },
                onSave = { updatedRule ->
                    rules = rules.map { rule ->
                        if (rule.id == updatedRule.id) updatedRule else rule
                    }
                    editingRuleId = null
                }
            )
        } else {
            LocationLambdaHomeScreen(
                rules = rules,
                maxRules = maxRules,
                onEditRule = { rule -> editingRuleId = rule.id }
            )
        }
    }
}
