package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.PipelineRun

@Composable
fun PipelineRunList(
    runs: List<PipelineRun>,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (runs.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pipeline runs.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(runs, key = { it.id }) { run ->
                PipelineRunCard(
                    run = run,
                    isExpanded = run.id in expandedIds,
                    onToggleExpand = { onToggleExpand(run.id) },
                )
            }
        }
    }
}
