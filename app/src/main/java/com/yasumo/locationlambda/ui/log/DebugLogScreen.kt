package com.yasumo.locationlambda.ui.log

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
import com.yasumo.locationlambda.debug.DebugLogEntry
import com.yasumo.locationlambda.debug.DebugLogRepository
import com.yasumo.locationlambda.debug.DebugLogType
import com.yasumo.locationlambda.ui.theme.CardSurface
import com.yasumo.locationlambda.ui.theme.Slate
import com.yasumo.locationlambda.ui.theme.SlateSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebugLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { DebugLogRepository(context) }
    var logs by remember { mutableStateOf(repository.loadVisibleLogs().asReversed()) }
    var showLogHelp by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = CardSurface) {
                val bottomScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(bottomScrollState)
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
                            logs = repository.loadVisibleLogs().asReversed()
                        }
                    )
                    LogDisplayClearButton(
                        onClick = {
                            repository.hideLogsBeforeNow()
                            logs = repository.loadVisibleLogs().asReversed()
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
                    text = "表示中のログはありません。",
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
        title = { Text(text = "ログ種類") },
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
                Text(text = "閉じる")
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
            text = "戻る",
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
            text = "？ ログ説明",
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
            text = "区切る",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF8A5A00)
        )
    }
}

@Composable
private fun LogDisplayClearButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFFF6E8E8))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "表示クリア",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF9F1D1D)
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
        DebugLogType.NOTIFICATION -> "通知の表示・失敗"
        DebugLogType.GEOFENCE -> "条件に合うジオフェンス反応"
        DebugLogType.IGNORED -> "クールダウンなどで無視"
        DebugLogType.REGISTRATION -> "ジオフェンス登録・解除"
        DebugLogType.PERMISSION -> "権限状態"
        DebugLogType.RECEIVED -> "受信した生イベント"
        DebugLogType.SUPPRESSED -> "再登録直後の発火抑制"
        DebugLogType.RULE -> "ルール保存・変更"
        DebugLogType.STATUS -> "端末状態"
        DebugLogType.RESTORE -> "再起動・更新後の復旧"
        DebugLogType.MARKER -> "手動で入れたログの区切り"
    }
}

private fun Long.toLogTime(): String {
    if (this <= 0L) return "----/--/-- --:--:--"
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date(this))
}
