package com.yasumo.locationlambda.ui.edit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yasumo.locationlambda.ui.theme.CardSurface
import com.yasumo.locationlambda.ui.theme.Divider
import com.yasumo.locationlambda.ui.theme.LocationLambdaTheme
import com.yasumo.locationlambda.ui.theme.Slate
import com.yasumo.locationlambda.ui.theme.SlateSoft
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

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
                                    focusedPlaceholderColor = MapPlaceholderGray,
                                    unfocusedPlaceholderColor = MapPlaceholderGray,
                                    disabledPlaceholderColor = MapPlaceholderGray,
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
