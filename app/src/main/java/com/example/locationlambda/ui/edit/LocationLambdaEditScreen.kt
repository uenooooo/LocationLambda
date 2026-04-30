package com.example.locationlambda.ui.edit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.locationlambda.notification.MockNotificationHelper
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Divider
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
    val context = LocalContext.current
    val appChoices = remember {
        listOf(
            AppChoice("Teams", "com.microsoft.teams"),
            AppChoice("Google Maps", "com.google.android.apps.maps"),
            AppChoice("Spotify", "com.spotify.music"),
            AppChoice("YouTube", "com.google.android.youtube")
        )
    }
    val mapChoices = remember {
        listOf(
            MapChoice("渋谷駅", "東京都渋谷区道玄坂1-1-1", "半径 150m"),
            MapChoice("会社", "東京都千代田区丸の内1-1-1", "半径 300m"),
            MapChoice("ジム", "東京都新宿区西新宿2-2-2", "半径 120m")
        )
    }

    var showAppDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var name by rememberSaveable(rule.id) { mutableStateOf(rule.name) }
    var address by rememberSaveable(rule.id) { mutableStateOf(rule.addressLabel) }
    var radiusLabel by rememberSaveable(rule.id) { mutableStateOf(rule.areaLabel) }
    var actionType by rememberSaveable(rule.id) { mutableStateOf(rule.actionTypeLabel) }
    var actionTargetLabel by rememberSaveable(rule.id) {
        mutableStateOf(if (rule.actionTargetLabel == "-") "" else rule.actionTargetLabel)
    }
    var actionTargetValue by rememberSaveable(rule.id) { mutableStateOf(rule.actionTargetValue) }
    var enabled by rememberSaveable(rule.id) { mutableStateOf(rule.enabled) }
    var onEnter by rememberSaveable(rule.id) {
        mutableStateOf(rule.transitions.any { it.label == "到着" })
    }
    var onExit by rememberSaveable(rule.id) {
        mutableStateOf(rule.transitions.any { it.label == "退出" })
    }

    fun buildEditedRule(): LocationRuleUi {
        val savedTargetLabel = when (actionType) {
            "なし" -> "-"
            "アプリを開く" -> actionTargetLabel.ifBlank { "-" }
            else -> actionTargetLabel.ifBlank { "-" }
        }
        val savedTargetValue = when (actionType) {
            "なし" -> ""
            "アプリを開く" -> actionTargetValue
            else -> actionTargetLabel
        }

        return rule.copy(
            name = name.ifBlank { "-" },
            addressLabel = address.ifBlank { "-" },
            areaLabel = radiusLabel.ifBlank { "-" },
            transitions = buildTransitions(onEnter, onExit),
            actionTypeLabel = actionType,
            actionTargetLabel = savedTargetLabel,
            actionTargetValue = savedTargetValue,
            enabled = enabled
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            MockNotificationHelper.showRuleNotification(
                context = context,
                rule = buildEditedRule()
            )
        } else {
            Toast.makeText(
                context,
                "通知権限がないため表示できません",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    if (showAppDialog) {
        AppPickerDialog(
            choices = appChoices,
            selectedPackageName = actionTargetValue,
            onDismiss = { showAppDialog = false },
            onSelect = { choice ->
                actionTargetLabel = choice.name
                actionTargetValue = choice.packageName
                showAppDialog = false
            }
        )
    }

    if (showMapDialog) {
        MapPickerDialog(
            choices = mapChoices,
            selectedAddress = address,
            onDismiss = { showMapDialog = false },
            onSelect = { choice ->
                name = choice.name
                address = choice.address
                radiusLabel = choice.radiusLabel
                showMapDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = CardSurface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InlineActionButton(label = "戻る", onClick = onBack)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InlineActionButton(
                            label = "通知",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                } else {
                                    MockNotificationHelper.showRuleNotification(
                                        context = context,
                                        rule = buildEditedRule()
                                    )
                                }
                            }
                        )
                        InlineActionButton(
                            label = "保存",
                            primary = true,
                            onClick = { onSave(buildEditedRule()) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ロケラム編集",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate
                    )
                    Text(
                        text = "場所とアクションを画面内でまとめて調整できます。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            }
            item {
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column {
                        SeamlessSection(title = "名前") {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        DividerLine()
                        SeamlessSection(title = "有効") {
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
                        DividerLine()
                        SeamlessSection(title = "場所と半径") {
                            MapSelectorRow(
                                name = name,
                                address = address,
                                radiusLabel = radiusLabel,
                                onClick = { showMapDialog = true }
                            )
                        }
                        DividerLine()
                        SeamlessSection(title = "タイミング") {
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
                        DividerLine()
                        SeamlessSection(title = "実行アクション") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ActionTypeChip(
                                        label = "URLを開く",
                                        selected = actionType == "URLを開く",
                                        onClick = {
                                            actionType = "URLを開く"
                                            if (actionTargetLabel == "-") actionTargetLabel = ""
                                            actionTargetValue = actionTargetLabel
                                        }
                                    )
                                    ActionTypeChip(
                                        label = "アプリを開く",
                                        selected = actionType == "アプリを開く",
                                        onClick = {
                                            actionType = "アプリを開く"
                                            if (actionTargetValue.isBlank()) {
                                                actionTargetLabel = ""
                                            }
                                        }
                                    )
                                    ActionTypeChip(
                                        label = "なし",
                                        selected = actionType == "なし",
                                        onClick = {
                                            actionType = "なし"
                                            actionTargetLabel = ""
                                            actionTargetValue = ""
                                        }
                                    )
                                }
                                when (actionType) {
                                    "アプリを開く" -> AppPickerRow(
                                        selectedLabel = actionTargetLabel,
                                        onClick = { showAppDialog = true }
                                    )
                                    "なし" -> DisabledTargetRow()
                                    else -> OutlinedTextField(
                                        value = actionTargetLabel,
                                        onValueChange = {
                                            actionTargetLabel = it
                                            actionTargetValue = it
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        label = { Text(text = "対象") },
                                        placeholder = { Text(text = "https://example.com") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeamlessSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
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

@Composable
private fun DividerLine() {
    HorizontalDivider(color = Divider)
}

@Composable
private fun AppPickerRow(
    selectedLabel: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "対象",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = selectedLabel.ifBlank { "アプリを選択" },
            style = MaterialTheme.typography.bodyLarge,
            color = Slate
        )
    }
}

@Composable
private fun DisabledTargetRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3EFE8))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "対象",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = "対象なし",
            style = MaterialTheme.typography.bodyLarge,
            color = SlateSoft
        )
    }
}

@Composable
private fun MapSelectorRow(
    name: String,
    address: String,
    radiusLabel: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "地図から場所を選択",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate
                )
                Text(
                    text = name.ifBlank { "-" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateSoft
                )
            }
            MiniInfoChip(label = "変更")
        }
        HorizontalDivider(color = Color(0xFFE8DED1))
        Text(
            text = "住所",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = address.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = Slate
        )
        Text(
            text = "半径",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = radiusLabel.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = Slate
        )
    }
}

@Composable
private fun MiniInfoChip(label: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFF8F4ED))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Slate
        )
    }
}

@Composable
private fun AppPickerDialog(
    choices: List<AppChoice>,
    selectedPackageName: String,
    onDismiss: () -> Unit,
    onSelect: (AppChoice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            InlineActionButton(label = "閉じる", onClick = onDismiss)
        },
        title = {
            Text(text = "アプリを選択")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                choices.forEach { choice ->
                    val selected = selectedPackageName == choice.packageName
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelect(choice) },
                        color = if (selected) Color(0xFFEAF3EE) else Color(0xFFF8F3EC),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = choice.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate
                            )
                            Text(
                                text = choice.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateSoft
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun MapPickerDialog(
    choices: List<MapChoice>,
    selectedAddress: String,
    onDismiss: () -> Unit,
    onSelect: (MapChoice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            InlineActionButton(label = "閉じる", onClick = onDismiss)
        },
        title = {
            Text(text = "場所を選択")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                choices.forEach { choice ->
                    val selected = selectedAddress == choice.address
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelect(choice) },
                        color = if (selected) Color(0xFFEAF3EE) else Color(0xFFF8F3EC),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = choice.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate
                            )
                            Text(
                                text = choice.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateSoft
                            )
                            Text(
                                text = choice.radiusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate
                            )
                        }
                    }
                }
            }
        }
    )
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

private data class AppChoice(
    val name: String,
    val packageName: String
)

private data class MapChoice(
    val name: String,
    val address: String,
    val radiusLabel: String
)

@Preview(showBackground = true)
@Composable
private fun LocationLambdaEditScreenPreview() {
    val previewRule = LocationRuleUi(
        id = "preview",
        name = "渋谷駅",
        addressLabel = "東京都渋谷区道玄坂1-1-1",
        areaLabel = "半径 150m",
        transitions = listOf(TransitionUi("到着", EnterBlue)),
        actionTypeLabel = "アプリを開く",
        actionTargetLabel = "Teams",
        actionTargetValue = "com.microsoft.teams",
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
