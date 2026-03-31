package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.ui.component.AgentCard
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.MetricsChart
import com.orchestradashboard.shared.ui.component.MetricsPointsChart
import com.orchestradashboard.shared.ui.component.StatusFilterBar
import com.orchestradashboard.shared.ui.component.TimeRangeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
    onAgentClick: ((String) -> Unit)? = null,
    onViewProjectsClick: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.startObserving() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Orchestra Dashboard") },
                actions = {
                    onViewProjectsClick?.let { onClick ->
                        TextButton(onClick = onClick) {
                            Text("View Projects")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            uiState.error?.let { error ->
                ErrorBanner(message = error, onDismiss = { viewModel.clearError() })
            }

            StatusFilterBar(
                selectedStatus = uiState.statusFilter,
                onStatusSelected = { viewModel.setStatusFilter(it) },
                modifier = Modifier.padding(vertical = 8.dp),
            )

            if (uiState.selectedAgent != null) {
                val chartState = uiState.metricsChart
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    chartState.selectedMetricName?.let { name ->
                        Text(name, style = MaterialTheme.typography.titleSmall)
                    }
                    TimeRangeSelector(
                        selected = chartState.selectedTimeRange,
                        onSelected = { viewModel.selectTimeRange(it) },
                    )
                    if (chartState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    } else if (uiState.timeSeriesData.isNotEmpty()) {
                        uiState.timeSeriesData.forEach { data ->
                            MetricsChart(
                                timeSeriesData = data,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    } else if (chartState.points.isNotEmpty()) {
                        MetricsPointsChart(
                            points = chartState.points,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                        )
                    }
                    chartState.error?.let { errorMsg ->
                        ErrorBanner(message = errorMsg, onDismiss = { viewModel.clearMetricsError() })
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.isLoading -> LoadingOverlay()
                    uiState.filteredAgents.isEmpty() -> EmptyState()
                    else ->
                        AgentGrid(
                            agents = uiState.filteredAgents,
                            selectedAgentId = uiState.selectedAgent?.id,
                            onAgentClick = { agent ->
                                viewModel.selectAgent(agent.id)
                                onAgentClick?.invoke(agent.id)
                            },
                        )
                }
            }
        }
    }
}

@Composable
private fun AgentGrid(
    agents: List<Agent>,
    selectedAgentId: String?,
    onAgentClick: (Agent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns =
            when {
                maxWidth < 600.dp -> 2
                maxWidth < 900.dp -> 3
                else -> 4
            }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(agents, key = { it.id }) { agent ->
                AgentCard(
                    agent = agent,
                    isSelected = agent.id == selectedAgentId,
                    onClick = { onAgentClick(agent) },
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No agents found.", style = MaterialTheme.typography.bodyLarge)
    }
}
