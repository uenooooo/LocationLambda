package com.example.locationlambda.permissions

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
        title = { Text(text = "\u4f4d\u7f6e\u60c5\u5831\u304c\u8a31\u53ef\u3055\u308c\u3066\u3044\u307e\u305b\u3093") },
        text = {
            Text(
                text = "\u4f4d\u7f6e\u60c5\u5831\u304c\u8a31\u53ef\u3055\u308c\u308b\u307e\u3067\u3001\u5834\u6240\u306b\u5165\u308b\u30fb\u51fa\u308b\u3068\u304d\u306e\u901a\u77e5\u306f\u52d5\u304d\u307e\u305b\u3093\u3002\u5f8c\u304b\u3089Android\u306e\u8a2d\u5b9a\u3067\u8a31\u53ef\u3067\u304d\u307e\u3059\u3002"
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(text = "\u8a2d\u5b9a\u3092\u958b\u304f")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "\u9589\u3058\u308b")
            }
        }
    )
}
