package com.yasumo.locationlambda.app

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yasumo.locationlambda.BuildConfig
import com.yasumo.locationlambda.data.ActionType
import com.yasumo.locationlambda.data.LocationRule
import com.yasumo.locationlambda.data.LocationTransition
import com.yasumo.locationlambda.data.createBlankRule
import com.yasumo.locationlambda.data.hasRegisteredLocation
import com.yasumo.locationlambda.debug.DebugDeviceStatusLogger
import com.yasumo.locationlambda.debug.DebugLogRepository
import com.yasumo.locationlambda.debug.DebugLogType
import com.yasumo.locationlambda.geofence.GeofenceManager
import com.yasumo.locationlambda.notification.GeofenceNotificationHelper
import com.yasumo.locationlambda.permissions.BackgroundLocationDialog
import com.yasumo.locationlambda.permissions.ForegroundLocationDialog
import com.yasumo.locationlambda.permissions.LocationPermissionDeniedDialog
import com.yasumo.locationlambda.permissions.NotificationPermissionDeniedDialog
import com.yasumo.locationlambda.permissions.PermissionStep
import com.yasumo.locationlambda.permissions.hasBackgroundLocationPermission
import com.yasumo.locationlambda.permissions.hasFineLocationPermission
import com.yasumo.locationlambda.permissions.hasGeofencePermissions
import com.yasumo.locationlambda.permissions.needsNotificationPermission
import com.yasumo.locationlambda.permissions.openAppSettings
import com.yasumo.locationlambda.permissions.openNotificationSettings
import com.yasumo.locationlambda.storage.RuleRepository
import com.yasumo.locationlambda.ui.edit.LocationLambdaEditScreen
import com.yasumo.locationlambda.ui.home.LocationLambdaHomeScreen
import com.yasumo.locationlambda.ui.log.DebugLogScreen
import com.yasumo.locationlambda.ui.model.toDomain
import com.yasumo.locationlambda.ui.model.toUi
import kotlinx.coroutines.delay

@Composable
internal fun LocationLambdaApp() {
    val context = LocalContext.current
    val repository = remember(context) { RuleRepository(context) }
    val debugLogRepository = remember(context) { DebugLogRepository(context) }
    val geofenceManager = remember(context) { GeofenceManager(context) }
    var showDebugLogScreen by remember { mutableStateOf(false) }
    var showIntroDialog by remember { mutableStateOf(true) }
    var rules by remember { mutableStateOf(repository.loadRules()) }
    var editingRuleId by remember { mutableStateOf<String?>(null) }
    var editingDraftRule by remember { mutableStateOf<LocationRule?>(null) }
    var permissionStep by remember { mutableStateOf(PermissionStep.Idle) }
    var showNotificationPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showForegroundLocationDialog by remember { mutableStateOf(false) }
    var showLocationPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showBackgroundLocationDialog by remember { mutableStateOf(false) }
    var geofenceRegistrationRequest by remember {
        mutableStateOf<GeofenceRegistrationRequest?>(null)
    }
    var geofenceRegistrationRequestSequence by remember { mutableStateOf(0L) }
    var hadGeofencePermissions by remember { mutableStateOf(context.hasGeofencePermissions()) }
    val maxRules = 5

    fun scheduleGeofenceReregistration(updatedRules: List<LocationRule>) {
        if (!context.hasGeofencePermissions()) return

        geofenceRegistrationRequestSequence += 1L
        geofenceRegistrationRequest =
                GeofenceRegistrationRequest(
                        id = geofenceRegistrationRequestSequence,
                        key = updatedRules.toGeofenceRegistrationKey()
                )
    }

    fun saveRules(updatedRules: List<LocationRule>) {
        val previousRules = rules
        val shouldReregisterGeofences =
                previousRules.toGeofenceRegistrationKey() !=
                        updatedRules.toGeofenceRegistrationKey()
        rules = updatedRules
        repository.saveRules(updatedRules)
        debugLogRepository.logRuleChanges(previousRules, updatedRules)
        if (shouldReregisterGeofences) {
            scheduleGeofenceReregistration(updatedRules)
        }
    }

    val notificationPermissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                DebugDeviceStatusLogger.logPermissions(
                        context = context,
                        title =
                                if (granted) "\u901a\u77e5\u6a29\u9650\u8a31\u53ef"
                                else "\u901a\u77e5\u6a29\u9650\u672a\u8a31\u53ef"
                )
                if (!granted && context.needsNotificationPermission()) {
                    showNotificationPermissionDeniedDialog = true
                }
                permissionStep = PermissionStep.ForegroundLocation
            }
    val locationPermissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineGranted =
                        permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                context.hasFineLocationPermission()
                DebugDeviceStatusLogger.logPermissions(
                        context = context,
                        title =
                                if (fineGranted) "\u524d\u666f\u4f4d\u7f6e\u6a29\u9650\u8a31\u53ef"
                                else "\u524d\u666f\u4f4d\u7f6e\u6a29\u9650\u672a\u8a31\u53ef"
                )
                permissionStep =
                        if (fineGranted) {
                            PermissionStep.BackgroundLocation
                        } else {
                            showLocationPermissionDeniedDialog = true
                            PermissionStep.Idle
                        }
            }

    LaunchedEffect(Unit) {
        DebugDeviceStatusLogger.logPermissions(context, "\u8d77\u52d5\u6642")
        DebugDeviceStatusLogger.logStatus(context, "\u8d77\u52d5\u6642")
        permissionStep = PermissionStep.Notification
    }

    LaunchedEffect(permissionStep) {
        when (permissionStep) {
            PermissionStep.Idle -> Unit
            PermissionStep.Notification -> {
                permissionStep = PermissionStep.Idle
                if (context.needsNotificationPermission()) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permissionStep = PermissionStep.ForegroundLocation
                }
            }
            PermissionStep.ForegroundLocation -> {
                permissionStep = PermissionStep.Idle
                if (!context.hasFineLocationPermission()) {
                    hadGeofencePermissions = false
                    showForegroundLocationDialog = true
                } else {
                    permissionStep = PermissionStep.BackgroundLocation
                }
            }
            PermissionStep.BackgroundLocation -> {
                permissionStep = PermissionStep.Idle
                if (!context.hasBackgroundLocationPermission()) {
                    hadGeofencePermissions = false
                    DebugDeviceStatusLogger.logPermissions(
                            context,
                            "\u30d0\u30c3\u30af\u30b0\u30e9\u30a6\u30f3\u30c9\u4f4d\u7f6e\u672a\u8a31\u53ef"
                    )
                    showBackgroundLocationDialog = true
                } else {
                    DebugDeviceStatusLogger.logPermissions(
                            context,
                            "\u30d0\u30c3\u30af\u30b0\u30e9\u30a6\u30f3\u30c9\u4f4d\u7f6e\u8a31\u53ef"
                    )
                    if (!hadGeofencePermissions) {
                        scheduleGeofenceReregistration(rules)
                    }
                    hadGeofencePermissions = true
                }
            }
        }
    }

    LaunchedEffect(geofenceRegistrationRequest) {
        val request = geofenceRegistrationRequest ?: return@LaunchedEffect
        delay(1_000)

        val latestRules = repository.loadRules()
        if (context.hasGeofencePermissions() &&
                        latestRules.toGeofenceRegistrationKey() == request.key
        ) {
            geofenceManager.reregister(latestRules)
        }
    }

    if (showForegroundLocationDialog) {
        ForegroundLocationDialog(
                onDismiss = { showForegroundLocationDialog = false },
                onRequestPermission = {
                    showForegroundLocationDialog = false
                    locationPermissionLauncher.launch(
                            arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                    )
                }
        )
    }

    if (showNotificationPermissionDeniedDialog) {
        NotificationPermissionDeniedDialog(
                onDismiss = { showNotificationPermissionDeniedDialog = false },
                onOpenSettings = {
                    showNotificationPermissionDeniedDialog = false
                    context.openNotificationSettings()
                }
        )
    }

    if (showLocationPermissionDeniedDialog) {
        LocationPermissionDeniedDialog(
                onDismiss = { showLocationPermissionDeniedDialog = false },
                onOpenSettings = {
                    showLocationPermissionDeniedDialog = false
                    context.openAppSettings()
                }
        )
    }

    if (showBackgroundLocationDialog) {
        BackgroundLocationDialog(
                onDismiss = { showBackgroundLocationDialog = false },
                onOpenSettings = {
                    showBackgroundLocationDialog = false
                    context.openAppSettings()
                }
        )
    }

    if (showIntroDialog) {
        Dialog(
                onDismissRequest = { showIntroDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Location Lambdaへようこそ")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                                "指定した場所に到着・退出したときに\n" +
                                        "通知やアクションを実行できます。\n\n" +
                                        "使い方\n" +
                                        "1. ルールを作成\n" +
                                        "2. 地点を選択\n" +
                                        "3. 条件を設定\n" +
                                        "4. 通知や起動アプリ設定"
                        )
                    }

                    Button(
                            onClick = { showIntroDialog = false },
                            modifier = Modifier.fillMaxWidth()
                    ) { Text("はじめる") }
                }
            }
        }
    }

    if (BuildConfig.SHOW_DEBUG_TOOLS && showDebugLogScreen) {
        DebugLogScreen(onBack = { showDebugLogScreen = false })
        return
    }

    val editingRule = rules.firstOrNull { it.id == editingRuleId } ?: editingDraftRule
    if (editingRule != null) {
        LocationLambdaEditScreen(
                rule = editingRule.toUi(),
                onBack = {
                    editingRuleId = null
                    editingDraftRule = null
                },
                onRuleChange = { updatedRule ->
                    val updatedDomainRule = updatedRule.toDomain(editingRule)
                    val updatedRules =
                            if (rules.any { it.id == updatedDomainRule.id }) {
                                rules.map { rule ->
                                    if (rule.id == updatedDomainRule.id) updatedDomainRule else rule
                                }
                            } else {
                                (rules + updatedDomainRule).take(maxRules)
                            }
                    saveRules(updatedRules)
                }
        )
    } else {
        LocationLambdaHomeScreen(
                rules = rules.map { it.toUi() },
                maxRules = maxRules,
                showDebugTools = BuildConfig.SHOW_DEBUG_TOOLS,
                onOpenLog = {
                    if (BuildConfig.SHOW_DEBUG_TOOLS) {
                        showDebugLogScreen = true
                    }
                },
                onEditRule = { rule -> editingRuleId = rule.id },
                onEditEmptyRule = { slotNumber ->
                    val draftRule = createBlankRule(slotNumber = slotNumber, existingRules = rules)
                    editingDraftRule = draftRule
                    editingRuleId = draftRule.id
                },
                onToggleRule = { toggledRule, enabled ->
                    val updatedRules =
                            rules.map { rule ->
                                if (rule.id == toggledRule.id) {
                                    rule.copy(enabled = enabled && rule.hasRegisteredLocation())
                                } else {
                                    rule
                                }
                            }
                    saveRules(updatedRules)
                },
                onDebugNotify = { rule ->
                    if (BuildConfig.SHOW_DEBUG_TOOLS) {
                        GeofenceNotificationHelper.showRuleNotification(context, rule)
                    }
                }
        )
    }
}

private data class GeofenceRegistrationKey(val rules: List<GeofenceRuleRegistrationKey>)

private data class GeofenceRegistrationRequest(val id: Long, val key: GeofenceRegistrationKey)

private data class GeofenceRuleRegistrationKey(
        val id: String,
        val enabledForRegistration: Boolean,
        val latitude: Double?,
        val longitude: Double?,
        val radiusMeters: Float?,
        val transitionType: Int?
)

private fun List<LocationRule>.toGeofenceRegistrationKey(): GeofenceRegistrationKey {
    return GeofenceRegistrationKey(map { it.toGeofenceRuleRegistrationKey() })
}

private fun LocationRule.toGeofenceRuleRegistrationKey(): GeofenceRuleRegistrationKey {
    val enabledForRegistration = enabled && hasRegisteredLocation()
    return GeofenceRuleRegistrationKey(
            id = id,
            enabledForRegistration = enabledForRegistration,
            latitude = latitude.takeIf { enabledForRegistration },
            longitude = longitude.takeIf { enabledForRegistration },
            radiusMeters = radiusMeters.takeIf { enabledForRegistration },
            transitionType = transitionType.takeIf { enabledForRegistration }
    )
}

private fun DebugLogRepository.logRuleChanges(
        previousRules: List<LocationRule>,
        updatedRules: List<LocationRule>
) {
    if (!BuildConfig.SHOW_DEBUG_TOOLS) return

    val previousById = previousRules.associateBy { it.id }
    updatedRules.forEach { rule ->
        val previousRule = previousById[rule.id]
        if (previousRule == rule) return@forEach

        append(
                type = DebugLogType.RULE,
                title =
                        if (previousRule == null) {
                            "${rule.name.ifBlank { rule.id }} \u65b0\u898f\u4fdd\u5b58"
                        } else {
                            "${rule.name.ifBlank { rule.id }} \u4fdd\u5b58"
                        },
                detail = rule.toRuleLogDetail()
        )
    }

    previousRules.filter { previous -> updatedRules.none { it.id == previous.id } }.forEach {
            deletedRule ->
        append(
                type = DebugLogType.RULE,
                title = deletedRule.name.ifBlank { deletedRule.id },
                detail = "\u524a\u9664"
        )
    }
}

private fun LocationRule.toRuleLogDetail(): String {
    return listOf(
                    "enabled=${enabled.toOnOffLabel()}",
                    "location=${if (hasRegisteredLocation()) "\u3042\u308a" else "\u306a\u3057"}",
                    "radius=${radiusMeters.toInt()}m",
                    "transition=${transitionType.toRuleTransitionLabel()}",
                    "cooldown=${cooldownMin.toCooldownLabel()}",
                    "action=${actionType.toRuleActionLabel()}",
                    "target=${toActionTargetLogLabel()}"
            )
            .joinToString(" ")
}

private fun Int.toRuleTransitionLabel(): String {
    val labels = mutableListOf<String>()
    if (LocationTransition.includesEnter(this)) labels += "\u5230\u7740"
    if (LocationTransition.includesExit(this)) labels += "\u9000\u51fa"
    return labels.joinToString("/").ifBlank { "-" }
}

private fun ActionType.toRuleActionLabel(): String {
    return when (this) {
        ActionType.URL -> "URL\u3092\u958b\u304f"
        ActionType.APP -> "\u30a2\u30d7\u30ea\u3092\u958b\u304f"
        ActionType.NOTIFICATION_ONLY -> "\u901a\u77e5\u306e\u307f"
    }
}

private fun LocationRule.toActionTargetLogLabel(): String {
    return when (actionType) {
        ActionType.URL -> actionValue.ifBlank { "-" }
        ActionType.APP -> actionLabel.ifBlank { actionValue.ifBlank { "-" } }
        ActionType.NOTIFICATION_ONLY -> "-"
    }
}

private fun Int.toCooldownLabel(): String {
    return if (this <= 0) "\u306a\u3057" else "${this}\u5206"
}

private fun Boolean.toOnOffLabel(): String {
    return if (this) "ON" else "OFF"
}
