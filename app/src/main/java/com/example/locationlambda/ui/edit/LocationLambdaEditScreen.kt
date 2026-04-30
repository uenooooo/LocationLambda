package com.example.locationlambda.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft
import com.example.locationlambda.ui.theme.SuccessGreen

@Composable
fun LocationLambdaEditScreen(
    rule: LocationRuleUi,
    onBack: () -> Unit,
    onSave: (LocationRuleUi) -> Unit
) {
    var name by rememberSaveable(rule.id) { mutableStateOf(rule.name) }
    var address by rememberSaveable(rule.id) { mutableStateOf(rule.addressLabel) }
    var radius by rememberSaveable(rule.id) { mutableStateOf(rule.areaLabel.filter(Char::isDigit)) }
    var actionType by rememberSaveable(rule.id) { mutableStateOf(rule.actionTypeLabel) }
    var actionTarget by rememberSaveable(rule.id) {
        mutableStateOf(if (rule.actionTargetLabel == "-") "" else rule.actionTargetLabel)
    }
    var enabled by rememberSaveable(rule.id) { mutableStateOf(rule.enabled) }
    var onEnter by rememberSaveable(rule.id) {
        mutableStateOf(rule.transitions.any { it.label == "到着" })
    }
    var onExit by rememberSaveable(rule.id) {
        mutableStateOf(rule.transitions.any { it.label == "退出" })
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InlineActionButton(label = "戻る", onClick = onBack)
                    InlineActionButton(
                        label = "保存",
                        primary = true,
                        onClick = {
                            val savedTarget = if (actionType == "なし") "-" else actionTarget.ifBlank { "-" }
                            onSave(
                                rule.copy(
                                    name = name.ifBlank { "-" },
                                    addressLabel = address.ifBlank { "-" },
                                    areaLabel = if (radius.isBlank()) "-" else "半径${radius}m",
                                    transitions = buildTransitions(onEnter, onExit),
                                    actionTypeLabel = actionType,
                                    actionTargetLabel = savedTarget,
                                    enabled = enabled
                                )
                            )
                        }
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ロケラム編集",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate
                    )
                    Text(
                        text = "場所とアクションを1画面でまとめて調整できます。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            }
            item {
                FieldSection(title = "名前") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            item {
                FieldSection(title = "住所") {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            item {
                FieldSection(title = "半径") {
                    OutlinedTextField(
                        value = radius,
                        onValueChange = { radius = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        suffix = {
                            Text(text = "m")
                        }
                    )
                }
            }
            item {
                FieldSection(title = "タイミング") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SelectChip(
                            label = "到着",
                            selected = onEnter,
                            selectedColor = EnterBlue,
                            onClick = { onEnter = !onEnter }
                        )
                        SelectChip(
                            label = "退出",
                            selected = onExit,
                            selectedColor = ExitOrange,
                            onClick = { onExit = !onExit }
                        )
                    }
                }
            }
            item {
                FieldSection(title = "実行アクション") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionTypeChip(
                                label = "URLを開く",
                                selected = actionType == "URLを開く",
                                onClick = { actionType = "URLを開く" }
                            )
                            ActionTypeChip(
                                label = "アプリを開く",
                                selected = actionType == "アプリを開く",
                                onClick = { actionType = "アプリを開く" }
                            )
                            ActionTypeChip(
                                label = "なし",
                                selected = actionType == "なし",
                                onClick = { actionType = "なし" }
                            )
                        }
                        OutlinedTextField(
                            value = if (actionType == "なし") "" else actionTarget,
                            onValueChange = { actionTarget = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = actionType != "なし",
                            label = {
                                Text(text = "対象")
                            },
                            placeholder = {
                                Text(text = if (actionType == "アプリを開く") "Teams" else "https://example.com")
                            }
                        )
                    }
                }
            }
            item {
                FieldSection(title = "有効") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "このロケラムを有効にする",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Slate
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = CardSurface,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate
            )
            content()
        }
    }
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val background = if (selected) selectedColor.copy(alpha = 0.12f) else Color(0xFFF4EFE7)
    val textColor = if (selected) selectedColor else Slate

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
private fun ActionTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) Slate else Color(0xFFF4EFE7)
    val textColor = if (selected) CardSurface else Slate

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
private fun InlineActionButton(
    label: String,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    val background = if (primary) SuccessGreen else Color(0xFFF3EEE5)
    val textColor = if (primary) CardSurface else Slate

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

private fun buildTransitions(onEnter: Boolean, onExit: Boolean): List<TransitionUi> {
    val transitions = mutableListOf<TransitionUi>()
    if (onEnter) transitions += TransitionUi("到着", EnterBlue)
    if (onExit) transitions += TransitionUi("退出", ExitOrange)
    if (transitions.isEmpty()) transitions += TransitionUi("到着", EnterBlue)
    return transitions
}

@Preview(showBackground = true)
@Composable
private fun LocationLambdaEditScreenPreview() {
    val previewRule = LocationRuleUi(
        id = "preview",
        name = "渋谷駅",
        addressLabel = "東京都渋谷区道玄坂1-1-1",
        areaLabel = "半径150m",
        transitions = listOf(TransitionUi("到着", EnterBlue)),
        actionTypeLabel = "URLを開く",
        actionTargetLabel = "https://example.com",
        enabled = true
    )

    LocationLambdaTheme {
        LocationLambdaEditScreen(
            rule = previewRule,
            onBack = {},
            onSave = {}
        )
    }
}
