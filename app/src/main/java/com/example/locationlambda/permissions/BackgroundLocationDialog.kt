package com.example.locationlambda.permissions

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
        title = { Text(text = "\u4f4d\u7f6e\u60c5\u5831\u306e\u5e38\u6642\u8a31\u53ef\u304c\u5fc5\u8981\u3067\u3059") },
        text = {
            Text(
                text = "\u30ed\u30b1\u30e9\u30e0\u3092\u30a2\u30d7\u30ea\u3092\u9589\u3058\u3066\u3044\u308b\u9593\u3082\u52d5\u304b\u3059\u306b\u306f\u3001\u8a2d\u5b9a\u753b\u9762\u3067\u4f4d\u7f6e\u60c5\u5831\u3092\u300c\u5e38\u306b\u8a31\u53ef\u300d\u306b\u3057\u3066\u304f\u3060\u3055\u3044\u3002"
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(text = "\u8a2d\u5b9a\u3092\u958b\u304f")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "\u5f8c\u3067")
            }
        }
    )
}
