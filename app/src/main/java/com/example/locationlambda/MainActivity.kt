package com.example.locationlambda

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.locationlambda.data.ActionType
import com.example.locationlambda.data.LocationRule
import com.example.locationlambda.data.LocationTransition
import com.example.locationlambda.geofence.GeofenceManager
import com.example.locationlambda.storage.GeofenceStatusRepository
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

    override fun onResume() {
        super.onResume()
        GeofenceManager(this).reregister(RuleRepository(this).loadRules())
    }
}

@Composable
private fun LocationLambdaApp() {
    val context = LocalContext.current
    val repository = remember(context) { RuleRepository(context) }
    val statusRepository = remember(context) { GeofenceStatusRepository(context) }
    var geofenceStatus by remember(context) { mutableStateOf(statusRepository.loadStatus()) }
    val geofenceManager = remember(context) {
        GeofenceManager(context) { status ->
            geofenceStatus = status
        }
    }
    var showSplash by remember { mutableStateOf(true) }
    var rules by remember { mutableStateOf(repository.loadRules()) }
    var editingRuleId by remember { mutableStateOf<String?>(null) }
    var editingDraftRule by remember { mutableStateOf<LocationRule?>(null) }
    var permissionStep by remember { mutableStateOf(PermissionStep.Idle) }
    var showBackgroundLocationDialog by remember { mutableStateOf(false) }
    val maxRules = 5

    DisposableEffect(statusRepository) {
        val listener = statusRepository.registerListener { status ->
            geofenceStatus = status
        }
        onDispose {
            statusRepository.unregisterListener(listener)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
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
            PermissionStep.Idle
        }
    }

    LaunchedEffect(Unit) {
        if (!BuildConfig.DEBUG) {
            delay(1_500)
            showSplash = false
        }
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
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
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
            geofenceManager.reregister(rules)
        }
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
        LocationLambdaSplashScreen(
            onDismiss = {
                if (BuildConfig.DEBUG) {
                    showSplash = false
                }
            }
        )
    } else {
        val editingRule = rules.firstOrNull { it.id == editingRuleId } ?: editingDraftRule
        if (editingRule != null) {
            LocationLambdaEditScreen(
                rule = editingRule.toUi(),
                onBack = {
                    editingRuleId = null
                    editingDraftRule = null
                },
                onSave = { updatedRule ->
                    val updatedDomainRule = updatedRule.toDomain(editingRule)
                    val updatedRules = if (rules.any { it.id == updatedDomainRule.id }) {
                        rules.map { rule ->
                            if (rule.id == updatedDomainRule.id) updatedDomainRule else rule
                        }
                    } else {
                        (rules + updatedDomainRule).take(maxRules)
                    }
                    rules = updatedRules
                    repository.saveRules(updatedRules)
                    if (context.hasGeofencePermissions()) {
                        geofenceManager.reregister(updatedRules)
                    }
                    editingRuleId = null
                    editingDraftRule = null
                }
            )
        } else {
            LocationLambdaHomeScreen(
                rules = rules.map { it.toUi() },
                geofenceStatus = geofenceStatus,
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
                            rule.copy(enabled = enabled)
                        } else {
                            rule
                        }
                    }
                    rules = updatedRules
                    repository.saveRules(updatedRules)
                    if (context.hasGeofencePermissions()) {
                        geofenceManager.reregister(updatedRules)
                    }
                }
            )
        }
    }
}

private fun createBlankRule(
    slotNumber: Int,
    existingRules: List<LocationRule>
): LocationRule {
    val id = (1..5)
        .map { it.toString() }
        .firstOrNull { candidate -> existingRules.none { it.id == candidate } }
        ?: "rule-$slotNumber"

    return LocationRule(
        id = id,
        name = "",
        address = "-",
        latitude = 0.0,
        longitude = 0.0,
        radiusMeters = 100f,
        transitionType = LocationTransition.ENTER,
        actionType = ActionType.NOTIFICATION_ONLY,
        actionValue = "",
        actionLabel = "",
        cooldownMin = 5,
        enabled = false,
        lastTriggeredAt = 0L
    )
}

@Composable
private fun BackgroundLocationDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "位置情報の常時許可が必要です") },
        text = {
            Text(
                text = "ロケラムをアプリを閉じている間も動かすには、設定画面で位置情報を「常に許可」にしてください。"
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(text = "設定を開く")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "後で")
            }
        }
    )
}

private enum class PermissionStep {
    Idle,
    Notification,
    ForegroundLocation,
    BackgroundLocation
}

private fun Context.needsNotificationPermission(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
}

private fun Context.hasFineLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.hasBackgroundLocationPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.hasGeofencePermissions(): Boolean {
    return hasFineLocationPermission() && hasBackgroundLocationPermission()
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
