package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.model.LogLevel
import com.orchestradashboard.shared.domain.model.LogStreamState
import com.orchestradashboard.shared.ui.logstream.LogStreamUiState
import kotlinx.coroutines.launch

private const val MAX_LOG_PANEL_HEIGHT = 320
private const val BOTTOM_THRESHOLD = 2
private val WARN_COLOR = Color(0xFFE08B0F)
private val ERROR_COLOR = Color(0xFFD32F2F)
private val DEBUG_COLOR = Color(0xFF9E9E9E)

@Composable
fun LogStreamPanel(
    uiState: LogStreamUiState,
    onStopStream: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(max = MAX_LOG_PANEL_HEIGHT.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        LogStreamPanelContent(uiState = uiState, onStopStream = onStopStream)
    }
}

@Composable
private fun LogStreamPanelContent(
    uiState: LogStreamUiState,
    onStopStream: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LogStreamPanelHeader(
            stepId = uiState.selectedStepId,
            onStopStream = onStopStream,
        )

        when (val state = uiState.streamState) {
            is LogStreamState.Loading -> LoadingContent()
            is LogStreamState.Error -> ErrorContent(message = state.message)
            else -> LogListContent(logs = uiState.logs)
        }
    }
}

@Composable
private fun LogStreamPanelHeader(
    stepId: String?,
    onStopStream: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stepId?.let { "Logs: $it" } ?: "Logs",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onStopStream) {
            Icon(Icons.Default.Close, contentDescription = "Stop streaming")
        }
    }
}

@Composable
private fun LoadingContent() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp))
        Text(
            text = "Connecting...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Text(
        text = "Stream error: $message",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp),
    )
}

@Composable
private fun LogListContent(logs: List<LogEntry>) {
    if (logs.isEmpty()) {
        Text(
            text = "No logs yet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isAtBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total == 0 || lastVisible >= total - BOTTOM_THRESHOLD
        }
    }

    LaunchedEffect(logs.size) {
        if (isAtBottom && logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.lastIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val fallback = MaterialTheme.colorScheme.onSurfaceVariant
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        ) {
            items(logs) { entry ->
                Text(
                    text = formatLogEntry(entry),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = logLevelColor(entry.level).takeOrElse { fallback },
                )
            }
        }

        if (!isAtBottom && logs.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(logs.lastIndex) }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to bottom")
            }
        }
    }
}

/**
 * Formats a [LogEntry] for display as a single line. The output contains the extracted
 * HH:mm:ss timestamp (when parseable from an ISO-8601 string), an optional level marker,
 * and the message.
 */
internal fun formatLogEntry(entry: LogEntry): String {
    val timePart = extractHmsOrEmpty(entry.timestamp)
    val levelMarker =
        when (entry.level) {
            LogLevel.INFO -> ""
            LogLevel.WARN -> " WARN"
            LogLevel.ERROR -> " ERROR"
            LogLevel.DEBUG -> " DEBUG"
        }
    val prefix =
        when {
            timePart.isEmpty() && levelMarker.isEmpty() -> ""
            timePart.isEmpty() -> "[${levelMarker.trim()}] "
            else -> "[$timePart$levelMarker] "
        }
    return "$prefix${entry.message}"
}

/**
 * Extracts the HH:mm:ss component from an ISO-8601 timestamp string. Returns an empty
 * string when the input does not contain a recognizable time component.
 */
private fun extractHmsOrEmpty(timestamp: String): String {
    if (timestamp.isBlank()) return ""
    val tIndex = timestamp.indexOf('T')
    val timeSection =
        if (tIndex >= 0 && tIndex + 1 < timestamp.length) {
            timestamp.substring(tIndex + 1)
        } else {
            timestamp
        }
    val hms = timeSection.take(8)
    val looksLikeHms = hms.length == 8 && hms[2] == ':' && hms[5] == ':'
    return if (looksLikeHms) hms else ""
}

/**
 * Returns a semantic color for a log level. [Color.Unspecified] means the caller should
 * fall back to the default on-surface text color (used for INFO).
 */
internal fun logLevelColor(level: LogLevel): Color =
    when (level) {
        LogLevel.INFO -> Color.Unspecified
        LogLevel.WARN -> WARN_COLOR
        LogLevel.ERROR -> ERROR_COLOR
        LogLevel.DEBUG -> DEBUG_COLOR
    }
