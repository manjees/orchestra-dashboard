package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.PeriodFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodFilterBar(
    selected: PeriodFilter,
    onSelected: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PeriodFilter.entries.forEach { period ->
            FilterChip(
                selected = period == selected,
                onClick = { onSelected(period) },
                label = { Text(period.label) },
            )
        }
    }
}
