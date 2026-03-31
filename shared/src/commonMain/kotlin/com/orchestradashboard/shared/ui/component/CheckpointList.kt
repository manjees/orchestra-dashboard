package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun CheckpointList(
    checkpoints: List<Checkpoint>,
    retryingCheckpointId: String?,
    onRetryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (checkpoints.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No failed checkpoints", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(checkpoints, key = { it.id }) { checkpoint ->
                CheckpointCard(
                    checkpoint = checkpoint,
                    isRetrying = retryingCheckpointId == checkpoint.id,
                    onRetryClick = { onRetryClick(checkpoint.id) },
                )
            }
        }
    }
}

@Composable
private fun CheckpointCard(
    checkpoint: Checkpoint,
    isRetrying: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = checkpoint.pipelineId,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = checkpoint.step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    CheckpointStatusBadge(checkpoint.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatRelativeTime(checkpoint.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (checkpoint.status == CheckpointStatus.FAILED) {
                OutlinedButton(
                    onClick = onRetryClick,
                    enabled = !isRetrying,
                ) {
                    if (isRetrying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckpointStatusBadge(
    status: CheckpointStatus,
    modifier: Modifier = Modifier,
) {
    val color =
        when (status) {
            CheckpointStatus.PASSED -> MaterialTheme.colorScheme.primary
            CheckpointStatus.FAILED -> MaterialTheme.colorScheme.error
            CheckpointStatus.RUNNING -> MaterialTheme.colorScheme.tertiary
            CheckpointStatus.PENDING -> MaterialTheme.colorScheme.secondary
            CheckpointStatus.UNKNOWN -> MaterialTheme.colorScheme.outline
        }
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

internal fun formatRelativeTime(instant: Instant): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val time = instant.toEpochMilliseconds()
    val diff = (now - time) / 1000

    return when {
        diff < 60 -> "Just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        else -> "${diff / 86400}d ago"
    }
}
