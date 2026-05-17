package com.yasumo.locationlambda.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.data.ActionType
import com.yasumo.locationlambda.data.LocationTransition
import com.yasumo.locationlambda.ui.model.LocationRuleUi
import com.yasumo.locationlambda.ui.theme.CardSurface
import com.yasumo.locationlambda.ui.theme.EnterBlue
import com.yasumo.locationlambda.ui.theme.ExitOrange
import kotlinx.coroutines.delay

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
                radiusLabel = "通知半径${result.radiusMeters.toInt()}m"
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
                        CompactSeamlessSection(title = "名前") {
                            TitleNameEditor(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        DividerLine()
                        SeamlessSection(title = "場所と通知半径") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                MapSelectorRow(
                                    address = address,
                                    radiusLabel = radiusLabel
                                )
                                FullWidthActionButton(
                                    label = "地図で選択",
                                    onClick = { showMapSelectionScreen = true }
                                )
                                FullWidthActionButton(
                                    label = "選択を削除",
                                    destructive = true,
                                    onClick = {
                                        address = "-"
                                        radiusLabel = "通知半径100m"
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
