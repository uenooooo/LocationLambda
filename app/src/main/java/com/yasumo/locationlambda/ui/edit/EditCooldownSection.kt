package com.yasumo.locationlambda.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.ui.theme.Slate
import com.yasumo.locationlambda.ui.theme.SlateSoft
import com.yasumo.locationlambda.ui.theme.SuccessGreen

@Composable
internal fun CooldownSelector(
    cooldownMin: Int,
    isCustomCooldown: Boolean,
    customCooldownText: String,
    onPresetSelected: (Int) -> Unit,
    onCustomSelected: () -> Unit,
    onCustomValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cooldownPresetMinutes.forEach { option ->
                CooldownChip(
                    minutes = option,
                    selected = !isCustomCooldown && cooldownMin == option,
                    modifier = Modifier.weight(1f),
                    onClick = { onPresetSelected(option) }
                )
            }
            CompactSelectChip(
                label = "\u30ab\u30b9\u30bf\u30e0",
                selected = isCustomCooldown,
                selectedColor = SuccessGreen,
                modifier = Modifier.weight(1.25f),
                onClick = onCustomSelected
            )
        }
        CustomCooldownField(
            value = if (isCustomCooldown) customCooldownText else cooldownMin.toString(),
            enabled = isCustomCooldown,
            onValueChange = onCustomValueChange
        )
    }
}

@Composable
private fun CustomCooldownField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    val background = if (enabled) Color(0xFFF4EFE7) else Color(0xFFF8F4EE)
    val textColor = if (enabled) Slate else SlateSoft.copy(alpha = 0.55f)
    val suffixColor = if (enabled) SlateSoft else SlateSoft.copy(alpha = 0.45f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(background)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                cursorBrush = SolidColor(SuccessGreen),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isBlank()) {
                                Text(
                                    text = "1\u301c720",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SlateSoft.copy(alpha = 0.55f)
                                )
                            }
                            innerTextField()
                        }
                        Text(
                            text = "\u5206",
                            style = MaterialTheme.typography.bodyMedium,
                            color = suffixColor
                        )
                    }
                }
            )
        }
        Text(
            text = "\u6700\u592712\u6642\u9593\u307e\u3067",
            style = MaterialTheme.typography.labelMedium,
            color = suffixColor
        )
    }
}

@Composable
private fun CooldownChip(
    minutes: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val label = if (minutes == 0) "\u306a\u3057" else "${minutes}\u5206"
    CompactSelectChip(
        label = label,
        selected = selected,
        selectedColor = SuccessGreen,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun CompactSelectChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) selectedColor.copy(alpha = 0.12f) else Color(0xFFF4EFE7)
    val textColor = if (selected) selectedColor else Slate

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
