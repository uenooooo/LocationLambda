package com.yasumo.locationlambda.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.R
import com.yasumo.locationlambda.ui.theme.CardSurface
import com.yasumo.locationlambda.ui.theme.Slate
import com.yasumo.locationlambda.ui.theme.SlateSoft
import com.yasumo.locationlambda.ui.theme.SuccessGreen

@Composable
internal fun HomeHeader(
    ruleCount: Int,
    activeCount: Int,
    maxRules: Int,
    onShowRequiredPermissions: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.location_lambda_icon),
                contentDescription = "Location Lambda",
                modifier = Modifier.size(70.dp)
            )
            Text(
                text = "\u30ed\u30b1\u30fc\u30b7\u30e7\u30f3\u30e9\u30e0\u30c0",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Slate
            )
        }
        Text(
            text = "\u5834\u6240\u306b\u5230\u7740\u30fb\u9000\u51fa\u3057\u305f\u3068\u304d\u306b\u901a\u77e5\u3068\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u8a2d\u5b9a\u3067\u304d\u307e\u3059\u3002",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateSoft
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    label = "\u8a2d\u5b9a ${ruleCount}/${maxRules}\u4ef6",
                    color = Slate,
                    textColor = CardSurface
                )
                StatPill(
                    label = "\u6709\u52b9${activeCount}\u4ef6",
                    color = SuccessGreen,
                    textColor = CardSurface
                )
            }
            HeaderPillButton(label = "\u6a29\u9650", onClick = onShowRequiredPermissions)
        }
    }
}

@Composable
private fun StatPill(label: String, color: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
private fun HeaderPillButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Slate)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = CardSurface
        )
    }
}
