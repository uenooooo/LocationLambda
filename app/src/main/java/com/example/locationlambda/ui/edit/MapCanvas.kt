package com.example.locationlambda.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.locationlambda.BuildConfig
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
internal fun MapLoadingPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFFE6EBF2)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Slate)
    }
}

@Composable
internal fun RealMap(
    modifier: Modifier = Modifier,
    name: String,
    radiusLabel: String,
    selectedPosition: LatLng,
    searchCameraTarget: LatLng?,
    onMapClick: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedPosition, 15f)
    }
    val markerState = remember { MarkerState(position = selectedPosition) }
    markerState.position = selectedPosition
    val radiusMeters = remember(radiusLabel) {
        radiusLabel.filter { it.isDigit() }
            .toDoubleOrNull()
            ?: 150.0
    }

    LaunchedEffect(searchCameraTarget) {
        val target = searchCameraTarget ?: return@LaunchedEffect
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            target,
            cameraPositionState.position.zoom
        )
    }

    if (BuildConfig.MAPS_API_KEY.isBlank()) {
        Box(
            modifier = modifier.background(Color(0xFFE6EBF2))
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                color = Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFD5DDE8))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Maps API\u30ad\u30fc\u3092\u8a2d\u5b9a\u3059\u308b\u3068\u5730\u56f3\u304c\u8868\u793a\u3055\u308c\u307e\u3059\u3002",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate
                    )
                    Text(
                        text = "local.properties \u306b MAPS_API_KEY=... \u3092\u8ffd\u52a0\u3057\u3066\u304f\u3060\u3055\u3044\u3002",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            }
        }
        return
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        onMapClick = onMapClick
    ) {
        Marker(
            state = markerState,
            title = name
        )
        Circle(
            center = selectedPosition,
            radius = radiusMeters,
            fillColor = Color(0x332D7FF9),
            strokeColor = Color(0xFF2D7FF9),
            strokeWidth = 3f
        )
    }
}
