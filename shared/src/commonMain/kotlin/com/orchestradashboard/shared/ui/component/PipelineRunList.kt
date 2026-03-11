package com.orchestradashboard.shared.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.PipelineStep
import com.orchestradashboard.shared.domain.model.StepStatus

@Composable
fun PipelineRunList(
    pipelineRuns: List<PipelineRun>,
    modifier: Modifier = Modifier,
) {
    if (pipelineRuns.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pipeline runs.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(pipelineRuns, key = { it.id }) { run ->
                PipelineRunCard(pipelineRun = run)
            }
        }
    }
}

@Composable
private fun PipelineRunCard(
    pipelineRun: PipelineRun,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PipelineStatusIcon(pipelineRun.status)
                    Spacer(Modifier.width(8.dp))
                    Text(pipelineRun.pipelineName, style = MaterialTheme.typography.titleMedium)
                }
                pipelineRun.duration?.let { durationMs ->
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Trigger: ${pipelineRun.triggerInfo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    pipelineRun.steps.forEach { step ->
                        StepRow(step = step)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(
    step: PipelineStep,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepStatusIcon(step.status)
            Spacer(Modifier.width(8.dp))
            Text(step.name, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = formatDuration(step.elapsedMs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PipelineStatusIcon(
    status: PipelineRunStatus,
    modifier: Modifier = Modifier,
) {
    val (text, color) =
        when (status) {
            PipelineRunStatus.QUEUED -> "Q" to Color(0xFF9E9E9E)
            PipelineRunStatus.RUNNING -> "R" to Color(0xFF2196F3)
            PipelineRunStatus.PASSED -> "P" to Color(0xFF4CAF50)
            PipelineRunStatus.FAILED -> "F" to Color(0xFFF44336)
            PipelineRunStatus.CANCELLED -> "C" to Color(0xFFFF9800)
        }
    Surface(
        modifier = modifier.size(24.dp),
        shape = RoundedCornerShape(4.dp),
        color = color,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun StepStatusIcon(
    status: StepStatus,
    modifier: Modifier = Modifier,
) {
    val (text, color) =
        when (status) {
            StepStatus.PENDING -> "..." to Color(0xFF9E9E9E)
            StepStatus.RUNNING -> "R" to Color(0xFF2196F3)
            StepStatus.PASSED -> "P" to Color(0xFF4CAF50)
            StepStatus.FAILED -> "F" to Color(0xFFF44336)
            StepStatus.SKIPPED -> "S" to Color(0xFFFF9800)
        }
    Surface(
        modifier = modifier.size(20.dp),
        shape = RoundedCornerShape(4.dp),
        color = color,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
