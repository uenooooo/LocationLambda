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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val repository = remember(context) { DebugLogRepository(context) }
    var logs by remember { mutableStateOf(repository.loadLogs().asReversed()) }
    var showLogHelp by remember { mutableStateOf(false) }

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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogBackButton(onClick = onBack)
                    LogHelpButton(onClick = { showLogHelp = true })
                    LogMarkerButton(
                        onClick = {
                            repository.appendMarker()
                            logs = repository.loadLogs().asReversed()
                        }
                    )
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

    if (showLogHelp) {
        LogHelpDialog(onDismiss = { showLogHelp = false })
    }
}

@Composable
private fun LogHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "\u30ed\u30b0\u7a2e\u985e") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DebugLogType.values().forEach { type ->
                    LogHelpLine(type = type)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "\u9589\u3058\u308b")
            }
        }
    )
}

@Composable
private fun LogHelpLine(type: DebugLogType) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelLarge,
            color = type.logColor()
        )
        Text(
            text = ":${type.description()}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate
        )
    }
}

@Composable
private fun DebugLogRow(log: DebugLogEntry) {
    val isMarker = log.type == DebugLogType.MARKER
    SelectionContainer {
        Text(
            text = log.toAnnotatedLogLine(),
            modifier = Modifier.padding(vertical = 3.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isMarker) log.type.logColor() else Slate,
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

@Composable
private fun LogHelpButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFEAF4EF))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\uff1f \u30ed\u30b0\u8aac\u660e",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF1F6B4A)
        )
    }
}

@Composable
private fun LogMarkerButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFFFF4D6))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "\u533a\u5207\u308b",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF8A5A00)
        )
    }
}

private fun DebugLogEntry.toAnnotatedLogLine() = buildAnnotatedString {
    val content = listOf(title, detail)
        .filter { it.isNotBlank() }
        .joinToString(" / ")
        .ifBlank { "-" }
    val lineColor = if (type == DebugLogType.MARKER) type.logColor() else Slate

    append(timestampMillis.toLogTime())
    append(" ")
    withStyle(SpanStyle(color = type.logColor())) {
        append(type.label)
    }
    append(" ")
    withStyle(SpanStyle(color = lineColor)) {
        append(content)
    }
}

private fun DebugLogType.logColor(): Color = when (this) {
    DebugLogType.NOTIFICATION -> Color(0xFF0E8F5B)
    DebugLogType.GEOFENCE -> Color(0xFFB45309)
    DebugLogType.IGNORED -> Color(0xFFDC2626)
    DebugLogType.REGISTRATION -> Color(0xFF7C2D12)
    DebugLogType.PERMISSION -> Color(0xFF047857)
    DebugLogType.RECEIVED -> Color(0xFF0891B2)
    DebugLogType.SUPPRESSED -> Color(0xFF64748B)
    DebugLogType.RULE -> Color(0xFF4F46E5)
    DebugLogType.STATUS -> Color(0xFF525252)
    DebugLogType.RESTORE -> Color(0xFFBE123C)
    DebugLogType.MARKER -> Color(0xFF8A5A00)
}

private fun DebugLogType.description(): String {
    return when (this) {
        DebugLogType.NOTIFICATION -> "\u901a\u77e5\u306e\u8868\u793a\u30fb\u5931\u6557"
        DebugLogType.GEOFENCE -> "\u6761\u4ef6\u306b\u5408\u3046\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9\u53cd\u5fdc"
        DebugLogType.IGNORED -> "\u30af\u30fc\u30eb\u30c0\u30a6\u30f3\u306a\u3069\u3067\u7121\u8996"
        DebugLogType.REGISTRATION -> "\u30b8\u30aa\u30d5\u30a7\u30f3\u30b9\u767b\u9332\u30fb\u89e3\u9664"
        DebugLogType.PERMISSION -> "\u6a29\u9650\u72b6\u614b"
        DebugLogType.RECEIVED -> "\u53d7\u4fe1\u3057\u305f\u751f\u30a4\u30d9\u30f3\u30c8"
        DebugLogType.SUPPRESSED -> "\u518d\u767b\u9332\u76f4\u5f8c\u306e\u767a\u706b\u6291\u5236"
        DebugLogType.RULE -> "\u30eb\u30fc\u30eb\u4fdd\u5b58\u30fb\u5909\u66f4"
        DebugLogType.STATUS -> "\u7aef\u672b\u72b6\u614b"
        DebugLogType.RESTORE -> "\u518d\u8d77\u52d5\u30fb\u66f4\u65b0\u5f8c\u306e\u5fa9\u65e7"
        DebugLogType.MARKER -> "\u624b\u52d5\u3067\u5165\u308c\u305f\u30ed\u30b0\u306e\u533a\u5207\u308a"
    }
}

private fun Long.toLogTime(): String {
    if (this <= 0L) return "----/--/-- --:--:--"
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date(this))
}
