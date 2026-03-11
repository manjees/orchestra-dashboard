package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType

@Composable
fun EventItem(
    event: AgentEvent,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = eventTypeColor(event.type).copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp),
        ) {
            Text(
                text = eventTypeLabel(event.type),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = eventTypeColor(event.type),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = formatRelativeTime(event.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = event.payload,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal fun eventTypeLabel(type: EventType): String =
    when (type) {
        EventType.HEARTBEAT -> "HB"
        EventType.STATUS_CHANGE -> "STATUS"
        EventType.PIPELINE_STARTED -> "START"
        EventType.PIPELINE_COMPLETED -> "DONE"
        EventType.ERROR -> "ERR"
    }

internal fun eventTypeColor(type: EventType): Color =
    when (type) {
        EventType.HEARTBEAT -> Color(0xFF2196F3)
        EventType.STATUS_CHANGE -> Color(0xFF9C27B0)
        EventType.PIPELINE_STARTED -> Color(0xFFFF9800)
        EventType.PIPELINE_COMPLETED -> Color(0xFF4CAF50)
        EventType.ERROR -> Color(0xFFF44336)
    }
