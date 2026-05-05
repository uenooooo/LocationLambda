package com.yasumo.locationlambda.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.ui.theme.CardSurface
import com.yasumo.locationlambda.ui.theme.Divider
import com.yasumo.locationlambda.ui.theme.Slate
import com.yasumo.locationlambda.ui.theme.SlateSoft

@Composable
internal fun EmptyRuleRow(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateSoft
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Divider
                    )
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Divider
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = false,
                onCheckedChange = {},
                enabled = false,
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = CardSurface,
                    uncheckedTrackColor = Divider,
                    uncheckedBorderColor = Color.Transparent,
                    disabledUncheckedThumbColor = CardSurface,
                    disabledUncheckedTrackColor = Divider,
                    disabledUncheckedBorderColor = Color.Transparent
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "\u901a\u77e5\u5f8c\u30a2\u30af\u30b7\u30e7\u30f3",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate
            )
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyLarge,
                color = Divider
            )
            Text(
                text = "\u5bfe\u8c61",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate
            )
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyLarge,
                color = Divider
            )
        }
    }
}
