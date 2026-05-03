package com.example.locationlambda.ui.log

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.locationlambda.debug.DebugLogEntry
import com.example.locationlambda.debug.DebugLogRepository
import com.example.locationlambda.debug.DebugLogType
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
        val horizontalScrollState = rememberScrollState()
        val verticalScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = 14.dp,
                    top = innerPadding.calculateTopPadding() + 14.dp,
                    end = 14.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp
                )
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LogTypeLegend()

            if (logs.isEmpty()) {
                Text(
                    text = "\u30ed\u30b0\u306f\u307e\u3060\u3042\u308a\u307e\u305b\u3093\u3002",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateSoft
                )
            } else {
                logs.forEach { log ->
                    DebugLogRow(log = log)
                }
            }
        }
    }
}

@Composable
private fun LogTypeLegend() {
    Text(
        text = buildAnnotatedString {
            appendLegend(DebugLogType.NOTIFICATION, "\u901a\u77e5")
            append(" / ")
            appendLegend(DebugLogType.ACTION, "\u30a2\u30af\u30b7\u30e7\u30f3")
            append(" / ")
            appendLegend(DebugLogType.GEOFENCE, "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9")
            append(" / ")
            appendLegend(DebugLogType.IGNORED, "\u7121\u8996")
        },
        modifier = Modifier.padding(bottom = 6.dp),
        style = MaterialTheme.typography.bodySmall,
        color = SlateSoft,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
private fun DebugLogRow(log: DebugLogEntry) {
    SelectionContainer {
        Text(
            text = log.toAnnotatedLogLine(),
            modifier = Modifier.padding(vertical = 3.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Slate,
            maxLines = 1,
            softWrap = false
        )
    }
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

private fun DebugLogEntry.toAnnotatedLogLine() = buildAnnotatedString {
    val content = listOf(title, detail)
        .filter { it.isNotBlank() }
        .joinToString(" / ")
        .ifBlank { "-" }

    append(timestampMillis.toLogTime())
    append(" ")
    withStyle(SpanStyle(color = type.logColor())) {
        append(type.label)
    }
    append(" ")
    append(content)
}

private fun DebugLogType.logColor(): Color = when (this) {
    DebugLogType.NOTIFICATION -> Color(0xFF0E8F5B)
    DebugLogType.ACTION -> Color(0xFF2563EB)
    DebugLogType.GEOFENCE -> Color(0xFFB45309)
    DebugLogType.IGNORED -> Color(0xFFDC2626)
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendLegend(
    type: DebugLogType,
    description: String
) {
    withStyle(SpanStyle(color = type.logColor())) {
        append(type.label)
    }
    append(":")
    append(description)
}

private fun Long.toLogTime(): String {
    if (this <= 0L) return "----/--/-- --:--:--"
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date(this))
}
