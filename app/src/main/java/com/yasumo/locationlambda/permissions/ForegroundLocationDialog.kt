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
        title = { Text(text = "\u4f4d\u7f6e\u60c5\u5831\u306e\u8a31\u53ef\u304c\u5fc5\u8981\u3067\u3059") },
        text = {
            Text(
                text = "\u30ed\u30b1\u30e9\u30e0\u306f\u3001\u8a2d\u5b9a\u3057\u305f\u5834\u6240\u306b\u5165\u3063\u305f\u308a\u51fa\u305f\u308a\u3057\u305f\u3053\u3068\u3092\u5224\u5b9a\u3059\u308b\u305f\u3081\u306b\u4f4d\u7f6e\u60c5\u5831\u3092\u4f7f\u3044\u307e\u3059\u3002\u6b21\u306e\u78ba\u8a8d\u3067\u8a31\u53ef\u3057\u3066\u304f\u3060\u3055\u3044\u3002"
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text(text = "\u8a31\u53ef\u3059\u308b")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "\u5f8c\u3067")
            }
        }
    )
}
