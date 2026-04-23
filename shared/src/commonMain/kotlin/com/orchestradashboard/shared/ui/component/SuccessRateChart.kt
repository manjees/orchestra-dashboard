package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.PipelineAnalytics

@Composable
fun SuccessRateChart(
    summary: PipelineAnalytics?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Success Rate", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (summary == null) {
                Text(
                    "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Card
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DonutChart(
                    successRate = summary.successRate,
                    totalRuns = summary.totalRuns,
                    modifier = Modifier.size(120.dp).testTag("success_rate_chart"),
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatRow("Total", summary.totalRuns.toString())
                    StatRow("Failed", summary.failedRuns.toString())
                    StatRow("Avg duration", "${summary.avgDurationSec.toInt()}s")
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    successRate: Double,
    totalRuns: Int,
    modifier: Modifier = Modifier,
) {
    val successColor = MaterialTheme.colorScheme.primary
    val failureColor = MaterialTheme.colorScheme.error
    val backgroundTextColor = MaterialTheme.colorScheme.onSurface
    val clamped = successRate.coerceIn(0.0, 1.0)
    val label = if (totalRuns == 0) "No runs" else "${(clamped * 100).toInt()}%"

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = size.minDimension * 0.15f
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            val successSweep = (clamped * 360f).toFloat()
            val failureSweep = 360f - successSweep

            drawArc(
                color = successColor,
                startAngle = -90f,
                sweepAngle = successSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth),
            )
            if (failureSweep > 0f) {
                drawArc(
                    color = failureColor,
                    startAngle = -90f + successSweep,
                    sweepAngle = failureSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeWidth),
                )
            }
        }
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = backgroundTextColor,
                ),
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
