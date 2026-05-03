package com.example.locationlambda

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.locationlambda.app.LocationLambdaApp
import com.example.locationlambda.geofence.GeofenceManager
import com.example.locationlambda.storage.RuleRepository
import com.example.locationlambda.ui.theme.LocationLambdaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.rgb(244, 240, 232)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true
        setContent {
            LocationLambdaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LocationLambdaApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GeofenceManager(this).reregister(RuleRepository(this).loadRules())
    }
}
