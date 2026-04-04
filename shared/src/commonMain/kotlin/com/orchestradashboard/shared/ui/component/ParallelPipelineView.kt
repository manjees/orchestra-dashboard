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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.PipelineDependency

@Composable
fun ParallelPipelineView(
    pipelines: List<MonitoredPipeline>,
    dependencies: List<PipelineDependency> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    // Track each lane's pixel height independently to correctly position arrows
    // when lanes contain different numbers of steps.
    val laneHeights =
        remember(pipelines.size) {
            mutableStateListOf<Float>().apply { repeat(pipelines.size) { add(0f) } }
        }
    val laneSpacingPx = with(density) { 12.dp.toPx() }

    Row(modifier = modifier.fillMaxWidth()) {
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
                    Row(modifier = Modifier.testTag("lane_header_${pipeline.id}")) {
                        Text(
                            text = pipeline.id,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    StepTimeline(steps = pipeline.steps)
                }
            }
        }
    }
}
