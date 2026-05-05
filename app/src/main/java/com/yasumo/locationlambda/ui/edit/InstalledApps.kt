package com.yasumo.locationlambda.ui.edit

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process

fun loadInstalledApps(context: Context): List<AppChoice> {
    val launcherApps = context.getSystemService(LauncherApps::class.java)
    val activities = launcherApps.getActivityList(null, Process.myUserHandle())

    return activities
        .map { activityInfo ->
            val packageName = activityInfo.applicationInfo.packageName
            val label = activityInfo.label.toString().ifBlank { packageName }
            AppChoice(
                name = label,
                packageName = packageName,
                icon = activityInfo.getBadgedIcon(0)
            )
        }
        .distinctBy { it.packageName }
        .sortedWith(
            compareBy<AppChoice> { it.name.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
}
