package com.orchestradashboard.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus

@Composable
fun PipelineRunCard(
    run: PipelineRun,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onToggleExpand,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pipelineStatusIcon(run.status),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = run.pipelineName,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatDuration(run.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    run.steps.forEach { step ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stepStatusIcon(step.status),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = step.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = formatDuration(step.elapsedMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun formatDuration(ms: Long?): String {
    if (ms == null) return "running\u2026"
    val totalSeconds = ms / 1000
    return when {
        totalSeconds < 60 -> "${totalSeconds}s"
        totalSeconds < 3600 -> "${totalSeconds / 60}m ${totalSeconds % 60}s"
        else -> "${totalSeconds / 3600}h ${(totalSeconds % 3600) / 60}m"
    }
}

private fun pipelineStatusIcon(status: PipelineRunStatus): String =
    when (status) {
        PipelineRunStatus.QUEUED -> "\u23F3"
        PipelineRunStatus.RUNNING -> "\u25B6\uFE0F"
        PipelineRunStatus.PASSED -> "\u2705"
        PipelineRunStatus.FAILED -> "\u274C"
        PipelineRunStatus.CANCELLED -> "\u23F9\uFE0F"
    }

private fun stepStatusIcon(status: StepStatus): String =
    when (status) {
        StepStatus.PENDING -> "\u23F3"
        StepStatus.RUNNING -> "\u25B6\uFE0F"
        StepStatus.PASSED -> "\u2705"
        StepStatus.FAILED -> "\u274C"
        StepStatus.SKIPPED -> "\u23ED\uFE0F"
    }

internal fun pipelineStatusColor(status: PipelineRunStatus): Color =
    when (status) {
        PipelineRunStatus.QUEUED -> Color(0xFF9E9E9E)
        PipelineRunStatus.RUNNING -> Color(0xFF2196F3)
        PipelineRunStatus.PASSED -> Color(0xFF4CAF50)
        PipelineRunStatus.FAILED -> Color(0xFFF44336)
        PipelineRunStatus.CANCELLED -> Color(0xFFFF9800)
    }
