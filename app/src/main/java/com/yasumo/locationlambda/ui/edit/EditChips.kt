package com.yasumo.locationlambda.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.ui.theme.Slate

@Composable
internal fun SelectChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val background = if (selected) selectedColor.copy(alpha = 0.12f) else Color(0xFFF4EFE7)
    val textColor = if (selected) selectedColor else Slate

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
