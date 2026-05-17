package com.yasumo.locationlambda.ui.edit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.yasumo.locationlambda.ui.theme.CardSurface
import com.yasumo.locationlambda.ui.theme.Slate
import com.yasumo.locationlambda.ui.theme.SlateSoft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun AppPickerRow(
    selectedLabel: String,
    selectedPackageName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appIcon = remember(selectedPackageName) {
        if (selectedPackageName.isBlank()) {
            null
        } else {
            runCatching {
                context.packageManager.getApplicationIcon(selectedPackageName)
            }.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ActionTargetMinHeight)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = if (selectedLabel.isBlank()) {
                "アプリを選択"
            } else {
                "対象"
            },
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBitmap = appIcon?.toBitmap()?.asImageBitmap()
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = selectedLabel,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                )
            }
            Text(
                text = selectedLabel.ifBlank { "アプリを選択" },
                style = MaterialTheme.typography.bodyLarge,
                color = Slate
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun UrlTargetRow(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .heightIn(min = ActionTargetMinHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = if (value.isBlank()) "URLを入力" else "対象",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            delay(250)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Slate),
            cursorBrush = SolidColor(Slate),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = "https://example.com",
                            style = MaterialTheme.typography.bodyLarge,
                            color = EditPlaceholderGray
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
internal fun DisabledTargetRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ActionTargetMinHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3EFE8))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "対象",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = "-",
            style = MaterialTheme.typography.bodyLarge,
            color = SlateSoft
        )
    }
}

@Composable
internal fun ActionTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) Slate else Color(0xFFF4EFE7)
    val textColor = if (selected) CardSurface else Slate

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}
