package com.example.locationlambda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.locationlambda.ui.home.LocationLambdaHomeScreen
import com.example.locationlambda.ui.splash.LocationLambdaSplashScreen
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationLambdaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LocationLambdaApp()
                }
            }
        }
    }
}

@Composable
private fun LocationLambdaApp() {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1_500)
        showSplash = false
    }

    if (showSplash) {
        LocationLambdaSplashScreen()
    } else {
        LocationLambdaHomeScreen()
    }
}
