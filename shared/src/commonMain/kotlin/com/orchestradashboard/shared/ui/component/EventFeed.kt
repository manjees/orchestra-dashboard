package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType

@Composable
fun EventFeed(
    events: List<AgentEvent>,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No events.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val sortedEvents = events.sortedByDescending { it.timestamp }
        val listState = rememberLazyListState()

        LaunchedEffect(sortedEvents.firstOrNull()?.id) {
            if (sortedEvents.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sortedEvents, key = { it.id }) { event ->
                EventItem(event = event)
            }
        }
    }
}

@Composable
private fun EventItem(
    event: AgentEvent,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EventTypeIcon(event.type)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.type.name.replace("_", " "),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = event.payload,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = formatRelativeTime(event.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EventTypeIcon(
    type: EventType,
    modifier: Modifier = Modifier,
) {
    val (text, color) =
        when (type) {
            EventType.HEARTBEAT -> "H" to Color(0xFF4CAF50)
            EventType.STATUS_CHANGE -> "S" to Color(0xFF2196F3)
            EventType.PIPELINE_STARTED -> "PS" to Color(0xFF9C27B0)
            EventType.PIPELINE_COMPLETED -> "PC" to Color(0xFF00BCD4)
            EventType.ERROR -> "E" to Color(0xFFF44336)
        }
    Surface(
        modifier = modifier.size(32.dp),
        shape = RoundedCornerShape(8.dp),
        color = color,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}
