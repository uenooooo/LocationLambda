package com.example.locationlambda.ui.log

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.locationlambda.debug.DebugLogEntry
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Slate
import com.example.locationlambda.ui.theme.SlateSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebugLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val logs = remember { DebugLogRepository(context).loadLogs() }

    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = CardSurface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogBackButton(onClick = onBack)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 14.dp,
                top = innerPadding.calculateTopPadding() + 14.dp,
                end = 14.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "\u30ed\u30b0\u306f\u307e\u3060\u3042\u308a\u307e\u305b\u3093\u3002",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateSoft
                    )
                }
            } else {
                items(logs) { log ->
                    DebugLogRow(log = log)
                }
            }
        }
    }
}

@Composable
private fun DebugLogRow(log: DebugLogEntry) {
    Text(
        text = log.toLogLine(),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 3.dp),
        style = MaterialTheme.typography.bodySmall,
        color = Slate,
        maxLines = 1
    )
}

@Composable
private fun LogBackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFF3EEE5))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u623b\u308b",
            style = MaterialTheme.typography.labelLarge,
            color = Slate
        )
    }
}

private fun DebugLogEntry.toLogLine(): String {
    val content = listOf(title, detail)
        .filter { it.isNotBlank() }
        .joinToString(" / ")
        .ifBlank { "-" }
    return "${timestampMillis.toLogTime()} ${type.label} $content"
}

private fun Long.toLogTime(): String {
    if (this <= 0L) return "----/--/-- --:--:--"
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date(this))
}
