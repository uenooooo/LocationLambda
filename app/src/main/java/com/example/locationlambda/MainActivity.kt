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
import androidx.compose.ui.platform.LocalContext
import com.example.locationlambda.storage.RuleRepository
import com.example.locationlambda.ui.edit.LocationLambdaEditScreen
import com.example.locationlambda.ui.home.LocationLambdaHomeScreen
import com.example.locationlambda.ui.model.toDomain
import com.example.locationlambda.ui.model.toUi
import com.example.locationlambda.ui.splash.LocationLambdaSplashScreen
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
    val context = LocalContext.current
    val repository = remember(context) { RuleRepository(context) }
    var showSplash by remember { mutableStateOf(true) }
    var rules by remember { mutableStateOf(repository.loadRules()) }
    var editingRuleId by remember { mutableStateOf<String?>(null) }
    val maxRules = 5

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
                rule = editingRule.toUi(),
                onBack = { editingRuleId = null },
                onSave = { updatedRule ->
                    val updatedDomainRule = updatedRule.toDomain(editingRule)
                    val updatedRules = rules.map { rule ->
                        if (rule.id == updatedDomainRule.id) updatedDomainRule else rule
                    }
                    rules = updatedRules
                    repository.saveRules(updatedRules)
                    editingRuleId = null
                }
            )
        } else {
            LocationLambdaHomeScreen(
                rules = rules.map { it.toUi() },
                maxRules = maxRules,
                onEditRule = { rule -> editingRuleId = rule.id }
            )
        }
    }
}
