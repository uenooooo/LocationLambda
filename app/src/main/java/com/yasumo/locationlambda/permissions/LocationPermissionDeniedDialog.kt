package com.yasumo.locationlambda.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun LocationPermissionDeniedDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "位置情報が許可されていません") },
        text = {
            Text(
                text = "位置情報が許可されるまで、場所に入る・出るときの通知は動きません。後からAndroidの設定で許可できます。"
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(text = "設定を開く")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "閉じる")
            }
        }
    )
}
