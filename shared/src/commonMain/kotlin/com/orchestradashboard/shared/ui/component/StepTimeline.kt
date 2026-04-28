package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.MonitoredStep
import com.orchestradashboard.shared.domain.model.StepStatus

@Composable
fun StepTimeline(
    steps: List<MonitoredStep>,
    modifier: Modifier = Modifier,
    selectedStepName: String? = null,
    onStepClick: ((String) -> Unit)? = null,
) {
    if (steps.isEmpty()) {
        Text(
            text = "No steps available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(16.dp),
        )
        return
    }

    LazyRow(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(steps) { index, step ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepNode(
                    step = step,
                    modifier = Modifier.width(72.dp),
                    isSelected = step.name == selectedStepName,
                    onClick = onStepClick?.let { handler -> { handler(step.name) } },
                )

                if (index < steps.lastIndex) {
                    StepConnector(step.status)
                }
            }
        }
    }
}

@Composable
private fun StepConnector(
    previousStepStatus: StepStatus,
    modifier: Modifier = Modifier,
) {
    val color =
        when (previousStepStatus) {
            StepStatus.PASSED -> stepStatusColor(StepStatus.PASSED)
            StepStatus.RUNNING -> stepStatusColor(StepStatus.RUNNING)
            else -> stepStatusColor(StepStatus.PENDING)
        }
    Box(
        modifier = modifier.width(24.dp).height(2.dp).background(color),
    )
}
