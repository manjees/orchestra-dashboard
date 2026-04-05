package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.PipelineDependency
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.ui.theme.DashboardTheme

@Composable
fun ParallelPipelineView(
    pipelines: List<MonitoredPipeline>,
    dependencies: List<PipelineDependency> = emptyList(),
    modifier: Modifier = Modifier,
) {
    if (pipelines.isEmpty()) {
        Text(
            text = "No parallel lanes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(16.dp).testTag("parallel_empty_state"),
        )
        return
    }

    val density = LocalDensity.current
    // Track each lane's pixel height independently to correctly position arrows
    // when lanes contain different numbers of steps.
    val laneHeights =
        remember(pipelines.size) {
            mutableStateListOf<Float>().apply { repeat(pipelines.size) { add(0f) } }
        }
    val laneSpacingPx = with(density) { 12.dp.toPx() }

    Column(modifier = modifier.fillMaxWidth()) {
        // Dependency legend (only when dependencies exist)
        if (dependencies.isNotEmpty()) {
            DependencyLegend(modifier = Modifier.testTag("dependency_legend"))
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            // Left gutter: arrow overlay (only rendered when dependencies exist)
            if (dependencies.isNotEmpty()) {
                DependencyArrowOverlay(
                    dependencies = dependencies,
                    pipelineIds = pipelines.map { it.id },
                    laneHeights = laneHeights.toList(),
                    laneSpacingPx = laneSpacingPx,
                    modifier =
                        Modifier
                            .width(40.dp)
                            .fillMaxHeight(),
                )
            }

            // Lane column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                pipelines.forEachIndexed { index, pipeline ->
                    Column(
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp)
                                .onGloballyPositioned { coords ->
                                    if (index < laneHeights.size) {
                                        laneHeights[index] = coords.size.height.toFloat()
                                    }
                                }
                                .testTag("lane_${pipeline.id}"),
                    ) {
                        LaneHeader(pipeline = pipeline)
                        StepTimeline(steps = pipeline.steps)
                    }
                }
            }
        }
    }
}

@Composable
private fun LaneHeader(pipeline: MonitoredPipeline) {
    Row(
        modifier = Modifier.testTag("lane_header_${pipeline.id}"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pipeline.id,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "#${pipeline.issueNum} ${pipeline.issueTitle}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).testTag("lane_issue_${pipeline.id}"),
        )
        LaneStatusBadge(pipeline = pipeline)
    }
}

@Composable
private fun LaneStatusBadge(pipeline: MonitoredPipeline) {
    val statusColors = DashboardTheme.statusColors
    val (color, label) =
        when (pipeline.status) {
            PipelineRunStatus.RUNNING -> MaterialTheme.colorScheme.primary to "RUNNING"
            PipelineRunStatus.PASSED -> statusColors.success to "PASSED"
            PipelineRunStatus.FAILED -> MaterialTheme.colorScheme.error to "FAILED"
            PipelineRunStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant to "CANCELLED"
            PipelineRunStatus.QUEUED -> MaterialTheme.colorScheme.onSurfaceVariant to "QUEUED"
        }
    Text(
        text = label,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.testTag("lane_status_${pipeline.id}"),
    )
}
