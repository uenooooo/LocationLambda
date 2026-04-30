package com.example.locationlambda.ui.edit

import android.content.Context
import android.content.Intent

fun loadInstalledApps(context: Context): List<AppChoice> {
    val packageManager = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    @Suppress("DEPRECATION")
    return packageManager.queryIntentActivities(launcherIntent, 0)
        .map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val label = resolveInfo.loadLabel(packageManager).toString().ifBlank { packageName }
            AppChoice(
                name = label,
                packageName = packageName,
                icon = resolveInfo.loadIcon(packageManager)
            )
        }
        .distinctBy { it.packageName }
        .sortedWith(
            compareBy<AppChoice> { it.name.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
}
