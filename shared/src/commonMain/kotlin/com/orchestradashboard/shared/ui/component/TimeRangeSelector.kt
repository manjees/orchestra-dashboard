package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    onSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = range == selected,
                onClick = { onSelected(range) },
                label = { Text(range.label) },
            )
        }
    }
}
