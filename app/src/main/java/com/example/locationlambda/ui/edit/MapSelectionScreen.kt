package com.example.locationlambda.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlambda.BuildConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Divider
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft

@Composable
fun MapSelectionScreen(
    name: String,
    address: String,
    radiusLabel: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RealMap(
            modifier = Modifier.fillMaxSize(),
            radiusLabel = radiusLabel
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "地図で選択",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate
                )
                Text(
                    text = "次に本物の地図APIへ置き換えます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateSoft
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = CardSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MapInfoRow(
                        label = "住所",
                        value = address.ifBlank { "-" }
                    )
                    HorizontalDivider(color = Divider)
                    MapInfoRow(
                        label = "半径",
                        value = radiusLabel.ifBlank { "-" }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapActionButton(
                        label = "戻る",
                        onClick = onBack
                    )
                    MapActionButton(
                        label = "この場所を使う",
                        primary = true,
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

@Composable
private fun RealMap(
    modifier: Modifier = Modifier,
    radiusLabel: String
) {
    val shibuya = remember { LatLng(35.658034, 139.701636) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(shibuya, 15f)
    }
    val markerState = remember { MarkerState(position = shibuya) }
    val radiusMeters = remember(radiusLabel) {
        radiusLabel.filter { it.isDigit() }
            .toDoubleOrNull()
            ?: 150.0
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
                        text = "Maps APIキーを設定すると地図が表示されます。",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate
                    )
                    Text(
                        text = "local.properties に MAPS_API_KEY=... を追加してください。",
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
        properties = MapProperties(isMyLocationEnabled = false)
    ) {
        Marker(
            state = markerState,
            title = "渋谷駅"
        )
        Circle(
            center = shibuya,
            radius = radiusMeters,
            fillColor = Color(0x332D7FF9),
            strokeColor = Color(0xFF2D7FF9),
            strokeWidth = 3f
        )
    }
}

@Composable
private fun MapInfoRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SlateSoft
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Slate
        )
    }
}

@Composable
private fun MapActionButton(
    label: String,
    primary: Boolean = false,
    onClick: () -> Unit
) {
    val background = if (primary) Color(0xFF3D8A64) else Color(0xFFF3EEE5)
    val textColor = if (primary) CardSurface else Slate

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapSelectionScreenPreview() {
    LocationLambdaTheme {
        MapSelectionScreen(
            name = "渋谷駅",
            address = "東京都渋谷区道玄坂1-1-1",
            radiusLabel = "半径 150m",
            onBack = {},
            onConfirm = {}
        )
    }
}
