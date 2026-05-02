package com.example.locationlambda.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlambda.R
import com.example.locationlambda.ui.theme.LocationLambdaTheme

@Composable
fun LocationLambdaSplashScreen(
    onDismiss: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFC))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.location_lambda_icon),
            contentDescription = "Location Lambda",
            modifier = Modifier
                .size(170.dp)
                .offset(y = (-24).dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationLambdaSplashScreenPreview() {
    LocationLambdaTheme {
        LocationLambdaSplashScreen()
    }
}
