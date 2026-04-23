package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.StepFailureRate

private val BUCKET_COLORS =
    listOf(
        Color(0xFFE8F5E9),
        Color(0xFFC8E6C9),
        Color(0xFFFFF9C4),
        Color(0xFFFFCC80),
        Color(0xFFEF9A9A),
    )

private fun failureBucketColor(rate: Double): Color =
    when {
        rate < 0.25 -> BUCKET_COLORS[0]
        rate < 0.50 -> BUCKET_COLORS[1]
        rate < 0.75 -> BUCKET_COLORS[2]
        rate < 1.0 -> BUCKET_COLORS[3]
        else -> BUCKET_COLORS[4]
    }

@Composable
fun StepFailureHeatmap(
    failures: List<StepFailureRate>,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().padding(8.dp).testTag("step_failure_heatmap")) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Step Failure Rates", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (failures.isEmpty()) {
                Text(
                    "No failure data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Card
            }

            val sorted = failures.sortedByDescending { it.failureRate }
            sorted.forEach { step ->
                StepFailureRow(step)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StepFailureRow(
    step: StepFailureRate,
    modifier: Modifier = Modifier,
) {
    val color = failureBucketColor(step.failureRate)
    val pct = (step.failureRate * 100).toInt()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = step.stepName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .background(color = color, shape = RoundedCornerShape(4.dp)),
        )
        Text(
            text = "${step.failedCount}/${step.totalCount} ($pct%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
