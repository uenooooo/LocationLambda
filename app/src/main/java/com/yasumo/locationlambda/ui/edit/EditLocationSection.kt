package com.yasumo.locationlambda.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.ui.theme.Slate
import com.yasumo.locationlambda.ui.theme.SlateSoft

@Composable
internal fun MapSelectorRow(
    address: String,
    radiusLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF7F2EA))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "住所",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = address.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = Slate
        )
        Text(
            text = "通知半径",
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = radiusLabel.toRadiusValueLabel(),
            style = MaterialTheme.typography.bodyLarge,
            color = Slate
        )
    }
}

private fun String.toRadiusValueLabel(): String {
    val meters = filter { it.isDigit() }
    return if (meters.isBlank()) "-" else "${meters}m"
}
