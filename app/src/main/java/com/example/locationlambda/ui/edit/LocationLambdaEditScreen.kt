package com.example.locationlambda.ui.edit

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.model.TransitionUi
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.EnterBlue
import com.example.locationlambda.ui.theme.ExitOrange
import com.example.locationlambda.ui.theme.LocationLambdaTheme
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
                        SeamlessSection(title = "\u5834\u6240\u3068\u901a\u77e5\u534a\u5f84") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                MapSelectorRow(
                                    address = address,
                                    radiusLabel = radiusLabel
                                )
                                FullWidthActionButton(
                                    label = "\u5730\u56f3\u3067\u9078\u629e",
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
                        SeamlessSection(title = "\u30bf\u30a4\u30df\u30f3\u30b0") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SelectChip(
                                    label = "\u5230\u7740",
                                    selected = onEnter,
                                    selectedColor = EnterBlue,
                                    onClick = { onEnter = !onEnter }
                                )
                                SelectChip(
                                    label = "\u9000\u51fa",
                                    selected = onExit,
                                    selectedColor = ExitOrange,
                                    onClick = { onExit = !onExit }
                                )
                            }
                        }
                        DividerLine()
                        SeamlessSection(title = "\u901a\u77e5\u30af\u30fc\u30eb\u30c0\u30a6\u30f3") {
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
                        SeamlessSection(title = "\u901a\u77e5\u5f8c\u30a2\u30af\u30b7\u30e7\u30f3") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ActionTypeChip(
                                        label = "URL\u3092\u958b\u304f",
                                        selected = actionTypeModel == ActionType.URL.name,
                                        onClick = {
                                            actionType = "URL\u3092\u958b\u304f"
                                            actionTypeModel = ActionType.URL.name
                                        }
                                    )
                                    ActionTypeChip(
                                        label = "\u30a2\u30d7\u30ea\u3092\u958b\u304f",
                                        selected = actionTypeModel == ActionType.APP.name,
                                        onClick = {
                                            actionType = "\u30a2\u30d7\u30ea\u3092\u958b\u304f"
                                            actionTypeModel = ActionType.APP.name
                                        }
                                    )
                                    ActionTypeChip(
                                        label = "\u306a\u3057",
                                        selected = actionTypeModel == ActionType.NOTIFICATION_ONLY.name,
                                        onClick = {
                                            actionType = "\u306a\u3057"
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

private fun buildTransitions(onEnter: Boolean, onExit: Boolean): List<TransitionUi> {
    val transitions = mutableListOf<TransitionUi>()
    if (onEnter) transitions += TransitionUi("\u5230\u7740", EnterBlue)
    if (onExit) transitions += TransitionUi("\u9000\u51fa", ExitOrange)
    if (transitions.isEmpty()) transitions += TransitionUi("\u5230\u7740", EnterBlue)
    return transitions
}

@Preview(showBackground = true)
@Composable
private fun LocationLambdaEditScreenPreview() {
    val previewRule = LocationRuleUi(
        id = "preview",
        name = "\u6e0b\u8c37\u99c5",
        addressLabel = "\u6771\u4eac\u90fd\u6e0b\u8c37\u533a\u9053\u7384\u57421-1-1",
        areaLabel = "\u901a\u77e5\u534a\u5f84150m",
        transitions = listOf(TransitionUi("\u5230\u7740", EnterBlue)),
        actionTypeLabel = "\u30a2\u30d7\u30ea\u3092\u958b\u304f",
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
