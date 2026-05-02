package com.example.locationlambda.ui.edit

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.data.LocationTransition
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val cooldownPresetMinutes = listOf(0, 5, 15, 30)
private const val maxCustomCooldownMinutes = 12 * 60

private fun hasRegisteredLocation(
    latitude: Double?,
    longitude: Double?,
    address: String
): Boolean {
    return latitude != null &&
        longitude != null &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        address.isNotBlank() &&
        address != "-"
}

@Composable
fun LocationLambdaEditScreen(
    rule: LocationRuleUi,
    onBack: () -> Unit,
    onRuleChange: (LocationRuleUi) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var showAppSelectionScreen by remember { mutableStateOf(false) }
    var showMapSelectionScreen by remember { mutableStateOf(false) }
    var name by rememberSaveable(rule.id) { mutableStateOf(rule.name) }
    var address by rememberSaveable(rule.id) { mutableStateOf(rule.addressLabel) }
    var radiusLabel by rememberSaveable(rule.id) { mutableStateOf(rule.areaLabel) }
    var latitude by rememberSaveable(rule.id) { mutableStateOf(rule.latitude) }
    var longitude by rememberSaveable(rule.id) { mutableStateOf(rule.longitude) }
    var radiusMeters by rememberSaveable(rule.id) { mutableStateOf(rule.radiusMeters) }
    var enabled by rememberSaveable(rule.id) { mutableStateOf(rule.enabled) }
    var actionType by rememberSaveable(rule.id) { mutableStateOf(rule.actionTypeLabel) }
    var actionTypeModel by rememberSaveable(rule.id) { mutableStateOf(rule.actionType.name) }
    var urlTargetValue by rememberSaveable(rule.id) {
        mutableStateOf(
            if (rule.actionType == ActionType.URL) {
                rule.actionTargetValue.takeUnless { it == "-" }.orEmpty().ifBlank {
                    if (rule.actionTargetLabel == "-") "" else rule.actionTargetLabel
                }
            } else {
                ""
            }
        )
    }
    var appTargetLabel by rememberSaveable(rule.id) {
        mutableStateOf(
            if (rule.actionType == ActionType.APP && rule.actionTargetLabel != "-") {
                rule.actionTargetLabel
            } else {
                ""
            }
        )
    }
    var appTargetValue by rememberSaveable(rule.id) {
        mutableStateOf(
            if (rule.actionType == ActionType.APP) {
                rule.actionTargetValue
            } else {
                ""
            }
        )
    }
    var cooldownMin by rememberSaveable(rule.id) { mutableStateOf(rule.cooldownMin) }
    var isCustomCooldown by rememberSaveable(rule.id) {
        mutableStateOf(rule.cooldownMin !in cooldownPresetMinutes)
    }
    var customCooldownText by rememberSaveable(rule.id) {
        mutableStateOf(
            if (rule.cooldownMin !in cooldownPresetMinutes && rule.cooldownMin > 0) {
                rule.cooldownMin.coerceAtMost(maxCustomCooldownMinutes).toString()
            } else {
                "60"
            }
        )
    }
    var onEnter by rememberSaveable(rule.id) {
        mutableStateOf(LocationTransition.includesEnter(rule.transitionType))
    }
    var onExit by rememberSaveable(rule.id) {
        mutableStateOf(LocationTransition.includesExit(rule.transitionType))
    }

    BackHandler {
        when {
            showAppSelectionScreen -> showAppSelectionScreen = false
            showMapSelectionScreen -> showMapSelectionScreen = false
            else -> onBack()
        }
    }

    fun buildEditedRule(): LocationRuleUi {
        val savedActionType = ActionType.valueOf(actionTypeModel)
        val savedTargetLabel = when (savedActionType) {
            ActionType.NOTIFICATION_ONLY -> "-"
            ActionType.APP -> appTargetLabel.ifBlank { "-" }
            ActionType.URL -> urlTargetValue.ifBlank { "-" }
        }
        val savedTargetValue = when (savedActionType) {
            ActionType.NOTIFICATION_ONLY -> ""
            ActionType.APP -> appTargetValue
            ActionType.URL -> urlTargetValue
        }

        return rule.copy(
            name = name,
            addressLabel = address,
            areaLabel = radiusLabel,
            transitions = buildTransitions(onEnter, onExit),
            actionTypeLabel = actionType,
            actionTargetLabel = savedTargetLabel,
            actionTargetValue = savedTargetValue,
            enabled = enabled,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            transitionType = LocationTransition.fromFlags(onEnter, onExit),
            actionType = savedActionType,
            cooldownMin = cooldownMin
        )
    }

    fun updateCustomCooldown(input: String) {
        val filtered = input.filter { it.isDigit() }.take(3)
        val minutes = filtered.toIntOrNull()?.coerceAtMost(maxCustomCooldownMinutes)
        customCooldownText = minutes?.toString() ?: filtered
        cooldownMin = minutes ?: 0
    }

    LaunchedEffect(
        name,
        address,
        radiusLabel,
        latitude,
        longitude,
        radiusMeters,
        enabled,
        actionType,
        actionTypeModel,
        urlTargetValue,
        appTargetLabel,
        appTargetValue,
        cooldownMin,
        onEnter,
        onExit
    ) {
        onRuleChange(buildEditedRule())
    }

    if (showAppSelectionScreen) {
        AppSelectionScreen(
            selectedPackageName = appTargetValue,
            onBack = { showAppSelectionScreen = false },
            onSelect = { choice ->
                appTargetLabel = choice.name
                appTargetValue = choice.packageName
                showAppSelectionScreen = false
            }
        )
        return
    }

    if (showMapSelectionScreen) {
        MapSelectionScreen(
            name = name,
            address = address,
            radiusLabel = radiusLabel,
            latitude = latitude,
            longitude = longitude,
            onBack = { showMapSelectionScreen = false },
            onConfirm = { result ->
                val hadRegisteredLocation = hasRegisteredLocation(latitude, longitude, address)
                val selectedAddress = result.address.ifBlank { address }
                address = selectedAddress
                radiusLabel = "\u901a\u77e5\u534a\u5f84${result.radiusMeters.toInt()}m"
                latitude = result.latitude
                longitude = result.longitude
                radiusMeters = result.radiusMeters
                if (!hadRegisteredLocation &&
                    hasRegisteredLocation(result.latitude, result.longitude, selectedAddress)
                ) {
                    enabled = true
                }
                showMapSelectionScreen = false
            }
        )
        return
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
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
                Surface(
                    color = CardSurface,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column {
                        CompactSeamlessSection(title = "\u540d\u524d") {
                            TitleNameEditor(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        DividerLine()
                        SeamlessSection(title = "場所と通知半径") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                HiddenActionButton(
                                    label = "地図で選択",
                                    onClick = { showMapSelectionScreen = true }
                                )
                                MapSelectorRow(
                                    name = name,
                                    address = address,
                                    radiusLabel = radiusLabel
                                )
                                FullWidthActionButton(
                                    label = "地図で選択",
                                    onClick = { showMapSelectionScreen = true }
                                )
                                FullWidthActionButton(
                                    label = "\u9078\u629e\u3092\u524a\u9664",
                                    destructive = true,
                                    onClick = {
                                        address = "-"
                                        radiusLabel = "\u901a\u77e5\u534a\u5f84100m"
                                        latitude = null
                                        longitude = null
                                        radiusMeters = 100f
                                        enabled = false
                                    }
                                )
                            }
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
                        SeamlessSection(title = "通知クールダウン") {
                            CooldownSelector(
                                cooldownMin = cooldownMin,
                                isCustomCooldown = isCustomCooldown,
                                customCooldownText = customCooldownText,
                                onPresetSelected = { option ->
                                    isCustomCooldown = false
                                    cooldownMin = option
                                },
                                onCustomSelected = {
                                    isCustomCooldown = true
                                    updateCustomCooldown(customCooldownText.ifBlank { "60" })
                                },
                                onCustomValueChange = { updateCustomCooldown(it) }
                            )
                        }
                        DividerLine()
                        SeamlessSection(title = "通知後アクション") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ActionTypeChip(
                                        label = "URLを開く",
                                        selected = actionTypeModel == ActionType.URL.name,
                                        onClick = {
                                            actionType = "URLを開く"
                                            actionTypeModel = ActionType.URL.name
                                        }
                                    )
                                    ActionTypeChip(
                                        label = "アプリを開く",
                                        selected = actionTypeModel == ActionType.APP.name,
                                        onClick = {
                                            actionType = "アプリを開く"
                                            actionTypeModel = ActionType.APP.name
                                        }
                                    )
                                    ActionTypeChip(
                                        label = "なし",
                                        selected = actionTypeModel == ActionType.NOTIFICATION_ONLY.name,
                                        onClick = {
                                            actionType = "なし"
                                            actionTypeModel = ActionType.NOTIFICATION_ONLY.name
                                        }
                                    )
                                }
                                when (actionTypeModel) {
                                    ActionType.APP.name -> AppPickerRow(
                                        selectedLabel = appTargetLabel,
                                        selectedPackageName = appTargetValue,
                                        onClick = { showAppSelectionScreen = true }
                                    )
                                    ActionType.NOTIFICATION_ONLY.name -> DisabledTargetRow()
                                    else -> UrlTargetRow(
                                        value = urlTargetValue,
                                        onValueChange = { urlTargetValue = it },
                                        modifier = Modifier.fillMaxWidth()
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
private fun CompactSeamlessSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Slate
        )
        content()
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
private fun TitleNameEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Slate
        ),
        cursorBrush = SolidColor(Slate),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isBlank()) {
                    Text(
                        text = "名前を入力",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PlaceholderGray
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun AppPickerRow(
    selectedLabel: String,
    selectedPackageName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appIcon = remember(selectedPackageName) {
        if (selectedPackageName.isBlank()) {
            null
        } else {
            runCatching {
                context.packageManager.getApplicationIcon(selectedPackageName)
            }.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ActionTargetMinHeight)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = if (selectedLabel.isBlank()) {
                "\u30a2\u30d7\u30ea\u3092\u9078\u629e"
            } else {
                "\u5bfe\u8c61"
            },
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBitmap = appIcon?.toBitmap()?.asImageBitmap()
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = selectedLabel,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                )
            }
            Text(
                text = selectedLabel.ifBlank { "アプリを選択" },
                style = MaterialTheme.typography.bodyLarge,
                color = Slate
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun UrlTargetRow(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .heightIn(min = ActionTargetMinHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = if (value.isBlank()) "\u0055\u0052\u004c\u3092\u5165\u529b" else "\u5bfe\u8c61",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            delay(250)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Slate),
            cursorBrush = SolidColor(Slate),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = "https://example.com",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PlaceholderGray
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun DisabledTargetRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ActionTargetMinHeight)
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
            text = "-",
            style = MaterialTheme.typography.bodyLarge,
            color = SlateSoft
        )
    }
}

@Composable
private fun MapSelectorRow(
    name: String,
    address: String,
    radiusLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
            text = "通知半径",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = radiusLabel.toRadiusValueLabel(),
            style = MaterialTheme.typography.bodyLarge,
            color = Slate
        )
    }
}

private fun String.toRadiusValueLabel(): String {
    val meters = filter { it.isDigit() }
    return if (meters.isBlank()) "-" else "${meters}m"
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
private fun CooldownSelector(
    cooldownMin: Int,
    isCustomCooldown: Boolean,
    customCooldownText: String,
    onPresetSelected: (Int) -> Unit,
    onCustomSelected: () -> Unit,
    onCustomValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cooldownPresetMinutes.forEach { option ->
                CooldownChip(
                    minutes = option,
                    selected = !isCustomCooldown && cooldownMin == option,
                    modifier = Modifier.weight(1f),
                    onClick = { onPresetSelected(option) }
                )
            }
            CompactSelectChip(
                label = "\u30ab\u30b9\u30bf\u30e0",
                selected = isCustomCooldown,
                selectedColor = SuccessGreen,
                modifier = Modifier.weight(1.25f),
                onClick = onCustomSelected
            )
        }
        CustomCooldownField(
            value = if (isCustomCooldown) customCooldownText else cooldownMin.toString(),
            enabled = isCustomCooldown,
            onValueChange = onCustomValueChange
        )
    }
}

@Composable
private fun CustomCooldownField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    val background = if (enabled) Color(0xFFF4EFE7) else Color(0xFFF8F4EE)
    val textColor = if (enabled) Slate else SlateSoft.copy(alpha = 0.55f)
    val suffixColor = if (enabled) SlateSoft else SlateSoft.copy(alpha = 0.45f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(background)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                cursorBrush = SolidColor(SuccessGreen),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isBlank()) {
                                Text(
                                    text = "1〜720",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SlateSoft.copy(alpha = 0.55f)
                                )
                            }
                            innerTextField()
                        }
                        Text(
                            text = "\u5206",
                            style = MaterialTheme.typography.bodyMedium,
                            color = suffixColor
                        )
                    }
                }
            )
        }
        Text(
            text = "\u6700\u592712\u6642\u9593\u307e\u3067",
            style = MaterialTheme.typography.labelMedium,
            color = suffixColor
        )
    }
}

@Composable
private fun CooldownChip(
    minutes: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val label = if (minutes == 0) "\u306a\u3057" else "${minutes}\u5206"
    CompactSelectChip(
        label = label,
        selected = selected,
        selectedColor = SuccessGreen,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun CompactSelectChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) selectedColor.copy(alpha = 0.12f) else Color(0xFFF4EFE7)
    val textColor = if (selected) selectedColor else Slate

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
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
private fun FullWidthActionButton(
    label: String,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    val background = if (destructive) Color(0xFFF4EFE7) else Color(0xFF2F7D62)
    val textColor = if (destructive) Slate else CardSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
private fun HiddenActionButton(
    label: String,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    Unit
}

private fun buildTransitions(onEnter: Boolean, onExit: Boolean): List<TransitionUi> {
    val transitions = mutableListOf<TransitionUi>()
    if (onEnter) transitions += TransitionUi("\u5230\u7740", EnterBlue)
    if (onExit) transitions += TransitionUi("\u9000\u51fa", ExitOrange)
    if (transitions.isEmpty()) transitions += TransitionUi("\u5230\u7740", EnterBlue)
    return transitions
}

data class AppChoice(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null
)

private val PlaceholderGray = Color(0xFF9AA6AD)
private val ActionTargetMinHeight = 86.dp

@Preview(showBackground = true)
@Composable
private fun LocationLambdaEditScreenPreview() {
    val previewRule = LocationRuleUi(
        id = "preview",
        name = "渋谷駅",
        addressLabel = "東京都渋谷区道玄坂1-1-1",
        areaLabel = "通知半径 150m",
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
            onRuleChange = {}
        )
    }
}
