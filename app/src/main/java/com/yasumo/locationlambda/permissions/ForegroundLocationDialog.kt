package com.yasumo.locationlambda.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun ForegroundLocationDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "位置情報の許可が必要です") },
        text = {
            Text(
                text = "ロケラムは、設定した場所に入ったり出たりしたことを判定するために位置情報を使います。次の確認で許可してください。"
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text(text = "許可する")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "後で")
            }
        }
    )
}
