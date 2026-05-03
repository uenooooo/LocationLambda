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
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.createBlankRule
import com.example.locationlambda.data.hasRegisteredLocation
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
import com.example.locationlambda.ui.edit.LocationLambdaEditScreen
import com.example.locationlambda.ui.home.LocationLambdaHomeScreen
import com.example.locationlambda.ui.model.toDomain
import com.example.locationlambda.ui.model.toUi
import com.example.locationlambda.ui.splash.LocationLambdaSplashScreen
import kotlinx.coroutines.delay

@Composable
internal fun LocationLambdaApp() {
    val context = LocalContext.current
    val repository = remember(context) { RuleRepository(context) }
    val geofenceManager = remember(context) { GeofenceManager(context) }
    var showSplash by remember { mutableStateOf(true) }
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
        rules = updatedRules
        repository.saveRules(updatedRules)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
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
                    showBackgroundLocationDialog = true
                } else {
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
            }
        )
    }
}
