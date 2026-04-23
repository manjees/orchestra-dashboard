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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.DurationTrend

@Composable
fun DurationTrendsChart(
    trends: List<DurationTrend>,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Duration Trends", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (trends.isEmpty()) {
                Text(
                    "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Card
            }

            TrendLineChart(
                trends = trends,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("duration_trends_chart"),
            )

            Spacer(Modifier.height(4.dp))
            TrendAxisLabels(trends)
        }
    }
}

@Composable
private fun TrendLineChart(
    trends: List<DurationTrend>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        if (trends.isEmpty()) return@Canvas

        val minVal = trends.minOf { it.avgDurationSec }
        val maxVal = trends.maxOf { it.avgDurationSec }
        val valRange = (maxVal - minVal).coerceAtLeast(1.0)
        val n = trends.size

        val padding = 8f
        val drawWidth = size.width - padding * 2
        val drawHeight = size.height - padding * 2

        fun xFor(index: Int): Float =
            if (n == 1) {
                size.width / 2f
            } else {
                padding + index.toFloat() / (n - 1).toFloat() * drawWidth
            }

        fun yFor(v: Double): Float = size.height - padding - ((v - minVal) / valRange * drawHeight).toFloat()

        if (n >= 2) {
            for (i in 0 until n - 1) {
                drawLine(
                    color = lineColor,
                    start = Offset(xFor(i), yFor(trends[i].avgDurationSec)),
                    end = Offset(xFor(i + 1), yFor(trends[i + 1].avgDurationSec)),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round,
                )
            }
        }

        trends.forEachIndexed { i, trend ->
            drawCircle(
                color = dotColor,
                radius = 4f,
                center = Offset(xFor(i), yFor(trend.avgDurationSec)),
            )
        }
    }
}

@Composable
private fun TrendAxisLabels(trends: List<DurationTrend>) {
    if (trends.isEmpty()) return
    val first = trends.first().date.takeLast(5)
    val last = trends.last().date.takeLast(5)
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            first,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        if (trends.size > 2) {
            val mid = trends[trends.size / 2].date.takeLast(5)
            Text(
                mid,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
        }
        Text(
            last,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
