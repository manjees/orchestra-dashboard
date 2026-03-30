package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.TimeSeriesData
import com.orchestradashboard.shared.domain.model.TimeSeriesPoint

@Composable
fun MetricsChart(
    timeSeriesData: TimeSeriesData,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = timeSeriesData.metricName,
                style = MaterialTheme.typography.titleMedium,
            )

            if (timeSeriesData.dataPoints.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    timeSeriesData.average?.let { avg ->
                        Text(
                            text = "Avg: $avg",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    }
                    timeSeriesData.min?.let { min ->
                        Text(
                            text = "Min: $min",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    }
                    timeSeriesData.max?.let { max ->
                        Text(
                            text = "Max: $max",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LineChart(
                    dataPoints = timeSeriesData.dataPoints,
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("metrics_chart"),
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    dataPoints: List<TimeSeriesData.DataPoint>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas

        val minValue = dataPoints.minOf { it.value }
        val maxValue = dataPoints.maxOf { it.value }
        val valueRange = (maxValue - minValue).coerceAtLeast(1.0)
        val minTime = dataPoints.minOf { it.timestamp }
        val maxTime = dataPoints.maxOf { it.timestamp }
        val timeRange = (maxTime - minTime).coerceAtLeast(1L)

        val padding = 8f

        val drawWidth = size.width - padding * 2
        val drawHeight = size.height - padding * 2

        fun xFor(ts: Long) = padding + (ts - minTime).toFloat() / timeRange * drawWidth

        fun yFor(v: Double) = size.height - padding - ((v - minValue).toFloat() / valueRange.toFloat() * drawHeight)

        for (i in 0 until dataPoints.size - 1) {
            val x1 = xFor(dataPoints[i].timestamp)
            val y1 = yFor(dataPoints[i].value)
            val x2 = xFor(dataPoints[i + 1].timestamp)
            val y2 = yFor(dataPoints[i + 1].value)

            drawLine(
                color = lineColor,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun MetricsPointsChart(
    points: List<TimeSeriesPoint>,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.testTag("metrics_chart"),
    ) {
        if (points.size < 2) return@Canvas
        val sorted = points.sortedBy { it.timestamp }
        val minTime = sorted.first().timestamp.toFloat()
        val maxTime = sorted.last().timestamp.toFloat()
        val minVal = sorted.minOf { it.value }.toFloat()
        val maxVal = sorted.maxOf { it.value }.toFloat()
        val timeSpan = (maxTime - minTime).coerceAtLeast(1f)
        val valSpan = (maxVal - minVal).coerceAtLeast(1f)

        val path = Path()
        sorted.forEachIndexed { i, pt ->
            val x = ((pt.timestamp - minTime) / timeSpan) * size.width
            val y = size.height - ((pt.value.toFloat() - minVal) / valSpan) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Color(0xFF6200EE), style = Stroke(width = 2.dp.toPx()))
    }
}
