package com.example.locationlambda.app

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.locationlambda.BuildConfig
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.data.createBlankRule
import com.example.locationlambda.data.hasRegisteredLocation
import com.example.locationlambda.debug.DebugDeviceStatusLogger
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType
import com.example.locationlambda.geofence.GeofenceManager
import com.example.locationlambda.permissions.BackgroundLocationDialog
import com.example.locationlambda.permissions.ForegroundLocationDialog
import com.example.locationlambda.permissions.LocationPermissionDeniedDialog
import com.example.locationlambda.permissions.NotificationPermissionDeniedDialog
import com.example.locationlambda.permissions.PermissionStep
import com.example.locationlambda.permissions.hasFineLocationPermission
import com.example.locationlambda.permissions.hasBackgroundLocationPermission
import com.example.locationlambda.permissions.hasGeofencePermissions
import com.example.locationlambda.permissions.needsNotificationPermission
import com.example.locationlambda.permissions.openAppSettings
import com.example.locationlambda.permissions.openNotificationSettings
import com.example.locationlambda.storage.RuleRepository
import com.example.locationlambda.notification.GeofenceNotificationHelper
import com.example.locationlambda.ui.edit.LocationLambdaEditScreen
import com.example.locationlambda.ui.home.LocationLambdaHomeScreen
import com.example.locationlambda.ui.log.DebugLogScreen
import com.example.locationlambda.ui.model.toDomain
import com.example.locationlambda.ui.model.toUi
import com.example.locationlambda.ui.splash.LocationLambdaSplashScreen
import kotlinx.coroutines.delay

@Composable
internal fun LocationLambdaApp() {
    val context = LocalContext.current
    val repository = remember(context) { RuleRepository(context) }
    val debugLogRepository = remember(context) { DebugLogRepository(context) }
    val geofenceManager = remember(context) { GeofenceManager(context) }
    var showSplash by remember { mutableStateOf(true) }
    var showDebugLogScreen by remember { mutableStateOf(false) }
    var rules by remember { mutableStateOf(repository.loadRules()) }
    var editingRuleId by remember { mutableStateOf<String?>(null) }
    var editingDraftRule by remember { mutableStateOf<LocationRule?>(null) }
    var permissionStep by remember { mutableStateOf(PermissionStep.Idle) }
    var showNotificationPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showForegroundLocationDialog by remember { mutableStateOf(false) }
    var showLocationPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showBackgroundLocationDialog by remember { mutableStateOf(false) }
    val maxRules = 5

    fun saveRules(updatedRules: List<LocationRule>) {
        val previousRules = rules
        rules = updatedRules
        repository.saveRules(updatedRules)
        debugLogRepository.logRuleChanges(previousRules, updatedRules)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        DebugDeviceStatusLogger.logPermissions(
            context = context,
            title = if (granted) "\u901a\u77e5\u6a29\u9650\u8a31\u53ef" else "\u901a\u77e5\u6a29\u9650\u672a\u8a31\u53ef"
        )
        if (!granted && context.needsNotificationPermission()) {
            showNotificationPermissionDeniedDialog = true
        }
        permissionStep = PermissionStep.ForegroundLocation
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            context.hasFineLocationPermission()
        DebugDeviceStatusLogger.logPermissions(
            context = context,
            title = if (fineGranted) "\u524d\u666f\u4f4d\u7f6e\u6a29\u9650\u8a31\u53ef" else "\u524d\u666f\u4f4d\u7f6e\u6a29\u9650\u672a\u8a31\u53ef"
        )
        permissionStep = if (fineGranted) {
            PermissionStep.BackgroundLocation
        } else {
            showLocationPermissionDeniedDialog = true
            PermissionStep.Idle
        }
    }

    LaunchedEffect(Unit) {
        delay(1_500)
        showSplash = false
    }

    LaunchedEffect(showSplash) {
        if (!showSplash) {
            DebugDeviceStatusLogger.logPermissions(context, "\u8d77\u52d5\u6642")
            DebugDeviceStatusLogger.logStatus(context, "\u8d77\u52d5\u6642")
            permissionStep = PermissionStep.Notification
        }
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
                    showForegroundLocationDialog = true
                } else {
                    permissionStep = PermissionStep.BackgroundLocation
                }
            }
            PermissionStep.BackgroundLocation -> {
                permissionStep = PermissionStep.Idle
                if (!context.hasBackgroundLocationPermission()) {
                    DebugDeviceStatusLogger.logPermissions(context, "\u30d0\u30c3\u30af\u30b0\u30e9\u30a6\u30f3\u30c9\u4f4d\u7f6e\u672a\u8a31\u53ef")
                    showBackgroundLocationDialog = true
                } else {
                    DebugDeviceStatusLogger.logPermissions(context, "\u30d0\u30c3\u30af\u30b0\u30e9\u30a6\u30f3\u30c9\u4f4d\u7f6e\u8a31\u53ef")
                    geofenceManager.reregister(rules)
                }
            }
        }
    }

    LaunchedEffect(rules) {
        if (context.hasGeofencePermissions()) {
            delay(1_000)
            geofenceManager.reregister(rules)
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

    if (showSplash) {
        LocationLambdaSplashScreen()
        return
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
                val updatedRules = if (rules.any { it.id == updatedDomainRule.id }) {
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
                val draftRule = createBlankRule(
                    slotNumber = slotNumber,
                    existingRules = rules
                )
                editingDraftRule = draftRule
                editingRuleId = draftRule.id
            },
            onToggleRule = { toggledRule, enabled ->
                val updatedRules = rules.map { rule ->
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
            title = if (previousRule == null) {
                "${rule.name.ifBlank { rule.id }} \u65b0\u898f\u4fdd\u5b58"
            } else {
                "${rule.name.ifBlank { rule.id }} \u4fdd\u5b58"
            },
            detail = rule.toRuleLogDetail()
        )
    }

    previousRules
        .filter { previous -> updatedRules.none { it.id == previous.id } }
        .forEach { deletedRule ->
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
    ).joinToString(" ")
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
