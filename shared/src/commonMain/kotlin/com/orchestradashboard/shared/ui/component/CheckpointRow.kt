package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus

@Composable
fun CheckpointRow(
    checkpoint: Checkpoint,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${checkpoint.step} — ${checkpoint.status.name.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Pipeline: ${checkpoint.pipelineId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (checkpoint.status == CheckpointStatus.FAILED) {
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onRetryClick) {
                Text("Retry")
            }
        }
    }
}
