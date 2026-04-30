package com.example.locationlambda.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import com.example.locationlambda.ui.theme.SandBackground
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft

@Composable
fun LocationLambdaSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SandBackground,
                        Color(0xFFE8DDCC)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0x80FFFFFF))
                .padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Location Lambda",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Slate
            )
            Text(
                text = "場所をきっかけに、次の動作へ。",
                style = MaterialTheme.typography.bodyLarge,
                color = SlateSoft
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationLambdaSplashScreenPreview() {
    LocationLambdaTheme {
        LocationLambdaSplashScreen()
    }
}
