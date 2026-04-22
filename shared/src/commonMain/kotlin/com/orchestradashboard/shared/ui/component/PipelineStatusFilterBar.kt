package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.PipelineRunStatus

private val DISPLAY_STATUSES =
    listOf<PipelineRunStatus?>(null) +
        listOf(
            PipelineRunStatus.PASSED,
            PipelineRunStatus.FAILED,
            PipelineRunStatus.CANCELLED,
            PipelineRunStatus.RUNNING,
        )

@Composable
fun PipelineStatusFilterBar(
    selected: PipelineRunStatus?,
    onSelected: (PipelineRunStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(DISPLAY_STATUSES) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelected(status) },
                label = { Text(status?.name ?: "All") },
            )
        }
    }
}
