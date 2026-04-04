package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.DependencyType
import com.orchestradashboard.shared.domain.model.PipelineDependency
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Pure geometry: given lane positions compute start/end points of an arrow. */
data class ArrowEndpoints(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
)

/** Three points forming the arrowhead triangle. */
data class ArrowHeadPoints(
    val tip: Offset,
    val left: Offset,
    val right: Offset,
)

/**
 * Computes start/end coordinates for a dependency arrow between two lanes.
 * Arrows are drawn in the left gutter; x coordinates equal [arrowPadding].
 */
fun calcArrowEndpoints(
    sourceLaneIndex: Int,
    targetLaneIndex: Int,
    laneHeight: Float,
    laneStartY: Float,
    laneWidth: Float,
    arrowPadding: Float = 8f,
): ArrowEndpoints {
    val startY = laneStartY + (sourceLaneIndex * laneHeight) + (laneHeight / 2f)
    val endY = laneStartY + (targetLaneIndex * laneHeight) + (laneHeight / 2f)
    return ArrowEndpoints(
        startX = arrowPadding,
        startY = startY,
        endX = arrowPadding,
        endY = endY,
    )
}

/**
 * Computes the Y center of a lane given a list of per-lane heights and the spacing
 * between lanes. Accumulates heights of all preceding lanes before computing the
 * midpoint of the target lane.
 */
fun calcLaneCenterY(
    laneIndex: Int,
    laneHeights: List<Float>,
    laneSpacingPx: Float = 0f,
    laneStartY: Float = 0f,
): Float {
    var y = laneStartY
    for (i in 0 until laneIndex) {
        y += laneHeights.getOrElse(i) { 0f } + laneSpacingPx
    }
    y += laneHeights.getOrElse(laneIndex) { 0f } / 2f
    return y
}

/**
 * Computes the three points forming an arrowhead triangle at [tip], pointing
 * in the direction given by [angle] (radians). The two base corners are offset
 * by [size] pixels from the tip.
 */
fun calcArrowHeadPoints(
    tip: Offset,
    angle: Float,
    size: Float = 10f,
): ArrowHeadPoints {
    val leftAngle = angle + (PI / 6).toFloat()
    val rightAngle = angle - (PI / 6).toFloat()
    val left =
        Offset(
            x = tip.x - size * cos(leftAngle),
            y = tip.y - size * sin(leftAngle),
        )
    val right =
        Offset(
            x = tip.x - size * cos(rightAngle),
            y = tip.y - size * sin(rightAngle),
        )
    return ArrowHeadPoints(tip = tip, left = left, right = right)
}

/** Returns the display color for a given [DependencyType]. */
fun dependencyColor(type: DependencyType): Color =
    when (type) {
        DependencyType.BLOCKS_START -> Color(0xFFFF9800) // Orange / Amber
        DependencyType.PROVIDES_INPUT -> Color(0xFF03A9F4) // Light Blue / Cyan
    }

/**
 * A legend row explaining arrow colors for each [DependencyType].
 * Only shown when the parallel view has at least one dependency.
 */
@Composable
fun DependencyLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(color = dependencyColor(DependencyType.BLOCKS_START), label = "Blocks")
        LegendItem(color = dependencyColor(DependencyType.PROVIDES_INPUT), label = "Provides Input")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .background(color, CircleShape),
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

/**
 * Canvas overlay that draws curved arrows between parallel pipeline lanes.
 * Arrow Y positions are computed per-lane from [laneHeights] to correctly
 * handle lanes of different heights.
 *
 * @param dependencies   List of dependencies to visualise.
 * @param pipelineIds    Ordered list of lane IDs (determines lane index).
 * @param laneHeights    Per-lane pixel heights in order.
 * @param laneSpacingPx  Vertical gap between lanes in pixels.
 */
@Composable
fun DependencyArrowOverlay(
    dependencies: List<PipelineDependency>,
    pipelineIds: List<String>,
    laneHeights: List<Float>,
    laneSpacingPx: Float = 0f,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.testTag("dependency_arrows")) {
        dependencies.forEach { dep ->
            val sourceIdx = pipelineIds.indexOf(dep.sourceLaneId)
            val targetIdx = pipelineIds.indexOf(dep.targetLaneId)
            // Skip unknown or self-referencing lanes
            if (sourceIdx < 0 || targetIdx < 0 || sourceIdx == targetIdx) return@forEach

            val startX = 8f
            val endX = 8f
            val startY = calcLaneCenterY(sourceIdx, laneHeights, laneSpacingPx)
            val endY = calcLaneCenterY(targetIdx, laneHeights, laneSpacingPx)
            val color = dependencyColor(dep.type)

            val curvePath =
                Path().apply {
                    moveTo(startX, startY)
                    cubicTo(
                        startX + 24f,
                        startY,
                        endX + 24f,
                        endY,
                        endX,
                        endY,
                    )
                }
            drawPath(
                path = curvePath,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )

            val angle = if (targetIdx > sourceIdx) (PI / 2).toFloat() else (-PI / 2).toFloat()
            val headPoints =
                calcArrowHeadPoints(
                    tip = Offset(endX, endY),
                    angle = angle,
                )
            val headPath =
                Path().apply {
                    moveTo(headPoints.tip.x, headPoints.tip.y)
                    lineTo(headPoints.left.x, headPoints.left.y)
                    lineTo(headPoints.right.x, headPoints.right.y)
                    close()
                }
            drawPath(path = headPath, color = color)
        }
    }
}
