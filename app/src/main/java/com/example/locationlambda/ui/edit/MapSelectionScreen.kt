package com.example.locationlambda.ui.edit

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.locationlambda.BuildConfig
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Divider
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@Composable
fun MapSelectionScreen(
    name: String,
    address: String,
    radiusLabel: String,
    latitude: Double? = null,
    longitude: Double? = null,
    onBack: () -> Unit,
    onConfirm: (MapSelectionResult) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val detailsExpandedHeight = 224.dp
    val detailsCollapsedHeight = 20.dp
    val detailsExpandedHeightPx = with(density) { detailsExpandedHeight.toPx() }
    val detailsCollapsedHeightPx = with(density) { detailsCollapsedHeight.toPx() }
    var selectedRadiusLabel by remember(radiusLabel) {
        mutableStateOf(normalizeRadiusLabel(radiusLabel))
    }
    var searchQuery by remember { mutableStateOf("") }
    val hasRegisteredPosition = remember(latitude, longitude, address) {
        hasRegisteredPosition(latitude, longitude, address)
    }
    var selectedPosition by remember(latitude, longitude, address) {
        mutableStateOf(
            if (hasRegisteredPosition && latitude != null && longitude != null) {
                LatLng(latitude, longitude)
            } else {
                parseCoordinates(address)
            }
        )
    }
    var searchCameraTarget by remember { mutableStateOf<LatLng?>(null) }
    var detailsPanelHeightPx by remember {
        mutableFloatStateOf(detailsExpandedHeightPx)
    }
    var resolvedAddress by remember(address) {
        mutableStateOf(
            if (parseCoordinates(address) == null) normalizeAddressLabel(address) else ""
        )
    }
    val animatedDetailsPanelHeightPx by animateFloatAsState(
        targetValue = detailsPanelHeightPx,
        label = "detailsPanelHeight"
    )
    val animatedDetailsPanelHeight = with(density) { animatedDetailsPanelHeightPx.toDp() }

    LaunchedEffect(selectedPosition) {
        val position = selectedPosition ?: return@LaunchedEffect
        val geocoded = reverseGeocode(context, position)
        resolvedAddress = normalizeAddressLabel(geocoded ?: "")
    }

    LaunchedEffect(hasRegisteredPosition) {
        if (hasRegisteredPosition) return@LaunchedEffect
        val currentPosition = getCurrentPosition(context) ?: return@LaunchedEffect
        selectedPosition = currentPosition
        searchCameraTarget = currentPosition
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        val currentSelectedPosition = selectedPosition
        if (currentSelectedPosition == null) {
            MapLoadingPlaceholder(modifier = Modifier.fillMaxSize())
        } else {
            RealMap(
                modifier = Modifier.fillMaxSize(),
                name = name,
                radiusLabel = selectedRadiusLabel,
                selectedPosition = currentSelectedPosition,
                searchCameraTarget = searchCameraTarget,
                onMapClick = {
                    focusManager.clearFocus()
                    selectedPosition = it
                }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedDetailsPanelHeight)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            detailsPanelHeightPx = (detailsPanelHeightPx - delta)
                                .coerceIn(detailsCollapsedHeightPx, detailsExpandedHeightPx)
                        },
                        onDragStopped = {
                            val midpoint =
                                (detailsExpandedHeightPx + detailsCollapsedHeightPx) / 2f
                            detailsPanelHeightPx =
                                if (detailsPanelHeightPx >= midpoint) {
                                    detailsExpandedHeightPx
                                } else {
                                    detailsCollapsedHeightPx
                                }
                        }
                    ),
                color = CardSurface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 6.dp, bottom = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .background(Color(0xFFD5DDE8))
                            .padding(horizontal = 28.dp, vertical = 1.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = {
                                    Text(text = "場所を検索")
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Slate,
                                    unfocusedIndicatorColor = Divider,
                                    focusedPlaceholderColor = PlaceholderGray,
                                    unfocusedPlaceholderColor = PlaceholderGray,
                                    disabledPlaceholderColor = PlaceholderGray,
                                    cursorColor = Slate
                                )
                            )
                            MapActionButton(
                                label = "検索",
                                onClick = {
                                    focusManager.clearFocus()
                                    val keyword = searchQuery.trim()
                                    if (keyword.isNotBlank()) {
                                        coroutineScope.launch {
                                            val result = geocodeLocationName(context, keyword)
                                            if (result != null) {
                                                selectedPosition = result
                                                searchCameraTarget = result
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        MapInfoRow(
                            label = "住所",
                            value = resolvedAddress
                        )
                        HorizontalDivider(color = Divider)
                        Text(
                            text = "通知半径",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateSoft
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("100m", "150m", "200m", "250m", "300m").forEach { option ->
                                RadiusChip(
                                    label = option,
                                    selected = selectedRadiusLabel == option,
                                    onClick = { selectedRadiusLabel = option }
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = CardSurface
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 0.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapActionButton(
                        label = "キャンセル",
                        onClick = onBack
                    )
                    MapActionButton(
                        label = "この場所を使う",
                        primary = true,
                        onClick = {
                            onConfirm(
                                MapSelectionResult(
                                    latitude = selectedPosition?.latitude ?: return@MapActionButton,
                                    longitude = selectedPosition?.longitude ?: return@MapActionButton,
                                    address = resolvedAddress,
                                    radiusMeters = selectedRadiusLabel.toMetersFloat(),
                                    radiusLabel = selectedRadiusLabel
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

data class MapSelectionResult(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val radiusMeters: Float,
    val radiusLabel: String
)

private val PlaceholderGray = Color(0xFF9AA6AD)

@Composable
private fun MapLoadingPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFFE6EBF2))
    )
}

@Composable
private fun RealMap(
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
            text = value.ifBlank { "\n" },
            style = MaterialTheme.typography.bodyLarge,
            color = Slate,
            minLines = 2
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

@Composable
private fun RadiusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) Color(0xFF24424D) else Color(0xFFF3EEE5)
    val textColor = if (selected) CardSurface else Slate

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

private fun parseCoordinates(address: String): LatLng? {
    val parts = address.split(",").map { it.trim() }
    if (parts.size != 2) return null
    val latitude = parts[0].toDoubleOrNull() ?: return null
    val longitude = parts[1].toDoubleOrNull() ?: return null
    return LatLng(latitude, longitude)
}

private fun hasRegisteredPosition(
    latitude: Double?,
    longitude: Double?,
    address: String
): Boolean {
    return latitude != null &&
        longitude != null &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0 &&
        !(latitude == 0.0 && longitude == 0.0) &&
        address.isNotBlank() &&
        address != "-"
}

private fun normalizeAddressLabel(address: String): String {
    return address.removePrefix("日本、").removePrefix("日本 ").trim()
}

private fun normalizeRadiusLabel(radiusLabel: String): String {
    val meters = radiusLabel.filter { it.isDigit() }.toIntOrNull() ?: 100
    return "${meters}m"
}

private fun String.toMetersFloat(): Float {
    return filter { it.isDigit() }.toFloatOrNull() ?: 100f
}

private suspend fun getCurrentPosition(context: android.content.Context): LatLng? {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasFineLocation && !hasCoarseLocation) return null

    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    return runCatching {
        suspendCancellableCoroutine { continuation ->
            @Suppress("MissingPermission")
            locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                continuation.resume(location?.let { LatLng(it.latitude, it.longitude) })
            }.addOnFailureListener {
                continuation.resume(null)
            }.addOnCanceledListener {
                continuation.resume(null)
            }
        }
    }.getOrNull()
}

private suspend fun reverseGeocode(
    context: android.content.Context,
    position: LatLng
): String? {
    val geocoder = Geocoder(context, Locale.JAPAN)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(position.latitude, position.longitude, 1) { addresses ->
                continuation.resume(addresses.firstOrNull()?.toDisplayText())
            }
        }
    } else {
        withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(position.latitude, position.longitude, 1)
                    ?.firstOrNull()
                    ?.toDisplayText()
            }.getOrNull()
        }
    }
}

private suspend fun geocodeLocationName(
    context: android.content.Context,
    query: String
): LatLng? {
    val geocoder = Geocoder(context, Locale.JAPAN)
    val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(query, 1) { addresses ->
                continuation.resume(addresses.firstOrNull())
            }
        }
    } else {
        withContext(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 1)?.firstOrNull()
            }.getOrNull()
        }
    }

    return address?.let { LatLng(it.latitude, it.longitude) }
}

private fun Address.toDisplayText(): String {
    val lines = (0..maxAddressLineIndex)
        .mapNotNull { getAddressLine(it) }
        .filter { it.isNotBlank() }
    return lines.joinToString(" ")
}

@Preview(showBackground = true)
@Composable
private fun MapSelectionScreenPreview() {
    LocationLambdaTheme {
        MapSelectionScreen(
            name = "渋谷駅",
            address = "東京都渋谷区道玄坂1-1-1",
            radiusLabel = "150m",
            onBack = {},
            onConfirm = {}
        )
    }
}
