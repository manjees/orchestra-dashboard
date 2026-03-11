package com.orchestradashboard.desktop.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.orchestradashboard.desktop.ui.theme.toColor
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startObserving()
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            Text(
                text = "Orchestra Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp),
            )

            StatusFilterBar(
                selectedFilter = uiState.filter,
                onFilterSelected = { viewModel.setFilter(it) },
            )

            uiState.error?.let { error ->
                ErrorBanner(
                    message = error,
                    onDismiss = { viewModel.clearError() },
                )
            }

            when {
                uiState.isLoading -> LoadingOverlay()
                uiState.filteredAgents.isEmpty() -> EmptyAgentState(hasFilter = uiState.filter != null)
                else ->
                    AgentGrid(
                        agents = uiState.filteredAgents,
                        selectedAgent = uiState.selectedAgent,
                        onSelect = { viewModel.selectAgent(it.id) },
                    )
            }
        }
    }
}

@Composable
private fun StatusFilterBar(
    selectedFilter: Agent.AgentStatus?,
    onFilterSelected: (Agent.AgentStatus?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("All") },
            )
        }
        items(Agent.AgentStatus.entries.size) { index ->
            val status = Agent.AgentStatus.entries[index]
            FilterChip(
                selected = selectedFilter == status,
                onClick = {
                    onFilterSelected(if (selectedFilter == status) null else status)
                },
                label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun AgentGrid(
    agents: List<Agent>,
    selectedAgent: Agent?,
    onSelect: (Agent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(agents, key = { it.id }) { agent ->
            AgentCard(
                agent = agent,
                isSelected = selectedAgent?.id == agent.id,
                onClick = { onSelect(agent) },
            )
        }
    }
}

@Composable
private fun AgentCard(
    agent: Agent,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderModifier =
        if (isSelected) {
            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        } else {
            Modifier
        }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().then(borderModifier),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 2.dp,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AgentStatusIndicator(status = agent.status)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = agent.name,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = agent.type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = formatRelativeTime(agent.lastHeartbeat),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun AgentStatusIndicator(
    status: Agent.AgentStatus,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(status.toColor()),
    )
}

@Composable
private fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyAgentState(
    hasFilter: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (hasFilter) "No agents match the selected filter." else "No agents registered. Start an agent to see it here.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun formatRelativeTime(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val diffSeconds = (now - epochMs) / 1000
    return when {
        diffSeconds < 0 -> "just now"
        diffSeconds < 60 -> "${diffSeconds}s ago"
        diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
        diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
        else -> "${diffSeconds / 86400}d ago"
    }
}
