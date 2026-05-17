package com.yasumo.locationlambda.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun BackgroundLocationDialog(
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
