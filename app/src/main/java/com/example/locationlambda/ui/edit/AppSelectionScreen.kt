package com.example.locationlambda.ui.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Divider
import com.example.locationlambda.ui.theme.LocationLambdaTheme
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft

@Composable
fun AppSelectionScreen(
    selectedPackageName: String,
    onBack: () -> Unit,
    onSelect: (AppChoice) -> Unit
) {
    val context = LocalContext.current
    val apps by produceState(initialValue = emptyList<AppChoice>(), context) {
        value = loadInstalledApps(context)
    }
    var query by remember { mutableStateOf("") }
    val filteredApps = remember(apps, query, selectedPackageName) {
        val keyword = query.trim().lowercase()
        val matchedApps = if (keyword.isBlank()) {
            apps
        } else {
            apps.filter { app ->
                app.name.lowercase().contains(keyword) ||
                    app.packageName.lowercase().contains(keyword)
            }
        }

        matchedApps.sortedWith(
            compareByDescending<AppChoice> { it.packageName == selectedPackageName }
                .thenBy { it.name.lowercase() }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = CardSurface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackPillButton(onClick = onBack)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "アプリを選択",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate
                    )
                    Text(
                        text = "通知や到着時に開くアプリを選びます。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            }
            item {
                Column(modifier = Modifier.padding(bottom = 20.dp)) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(text = "アプリを検索")
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Slate,
                            unfocusedIndicatorColor = Divider,
                            cursorColor = Slate
                        )
                    )
                }
            }

            if (apps.isEmpty()) {
                item {
                    Text(
                        text = "アプリを読み込み中です。",
                        modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            } else if (filteredApps.isEmpty()) {
                item {
                    Text(
                        text = "一致するアプリがありません。",
                        modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            } else {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppSelectionRow(
                        app = app,
                        selected = app.packageName == selectedPackageName,
                        onClick = { onSelect(app) }
                    )
                    HorizontalDivider(color = Divider)
                }
            }
        }
    }
}

@Composable
private fun AppSelectionRow(
    app: AppChoice,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconBitmap = app.icon?.toBitmap()?.asImageBitmap()

        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = app.name,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Surface(
                modifier = Modifier.size(44.dp),
                color = Color(0xFFF3EEE5),
                shape = RoundedCornerShape(12.dp)
            ) {}
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Slate
            )
        }

        if (selected) {
            Text(
                text = "選択中",
                style = MaterialTheme.typography.labelLarge,
                color = Slate
            )
        }
    }
}

@Composable
private fun BackPillButton(
    onClick: () -> Unit
) {
    Text(
        text = "戻る",
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFF3EEE5))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.labelLarge,
        color = Slate
    )
}

@Preview(showBackground = true)
@Composable
private fun AppSelectionScreenPreview() {
    LocationLambdaTheme {
        AppSelectionScreen(
            selectedPackageName = "com.microsoft.teams",
            onBack = {},
            onSelect = {}
        )
    }
}
