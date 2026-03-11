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
import com.orchestradashboard.shared.domain.model.Agent

@Composable
fun StatusFilterBar(
    selectedStatus: Agent.AgentStatus?,
    onStatusSelected: (Agent.AgentStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf<Agent.AgentStatus?>(null) + Agent.AgentStatus.entries
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(options) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = {
                    Text(
                        status?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All",
                    )
                },
                leadingIcon =
                    if (status != null) {
                        { AgentStatusIndicator(status, size = 8.dp) }
                    } else {
                        null
                    },
            )
        }
    }
}
