package com.orchestradashboard.desktop.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.DashboardViewModel

/**
 * Main dashboard screen for the Desktop app.
 *
 * @param viewModel Dashboard ViewModel providing UI state
 * @param modifier Optional modifier for the root composable
 */
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

            when {
                uiState.isLoading -> LoadingOverlay()
                uiState.agents.isEmpty() -> EmptyAgentState()
                else ->
                    AgentList(
                        agents = uiState.agents,
                        onSelect = { viewModel.selectAgent(it.id) },
                    )
            }

            uiState.error?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AgentList(
    agents: List<Agent>,
    onSelect: (Agent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(agents, key = { it.id }) { agent ->
            AgentCard(agent = agent, onClick = { onSelect(agent) })
        }
    }
}

@Composable
private fun AgentCard(
    agent: Agent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = agent.displayName,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Status: ${agent.status.name}",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (agent.isHealthy) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
            Text(
                text = "Last heartbeat: ${agent.lastHeartbeat}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyAgentState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No agents registered. Start an agent to see it here.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
