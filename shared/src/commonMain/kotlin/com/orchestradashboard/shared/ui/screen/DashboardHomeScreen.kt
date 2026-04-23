package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.ui.component.ActivePipelineCard
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.PipelineResultRow
import com.orchestradashboard.shared.ui.component.QuickActionsBar
import com.orchestradashboard.shared.ui.component.SystemHealthBar
import com.orchestradashboard.shared.ui.dashboardhome.DashboardHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHomeScreen(
    viewModel: DashboardHomeViewModel,
    onNewSolveClick: () -> Unit,
    onViewProjectsClick: () -> Unit,
    onCommandCenterClick: () -> Unit,
    onPipelineClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
        viewModel.startObserving()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.DateRange, contentDescription = "History")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            state.error?.let { error ->
                ErrorBanner(message = error, onDismiss = { viewModel.clearError() })
            }

            when {
                state.isLoading -> LoadingOverlay()
                else ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item { SystemHealthBar(state.systemStatus) }

                        item {
                            QuickActionsBar(
                                onNewSolveClick = onNewSolveClick,
                                onViewProjectsClick = onViewProjectsClick,
                                onCommandCenterClick = onCommandCenterClick,
                            )
                        }

                        if (state.hasActivePipelines) {
                            item { SectionHeader("Active Pipelines") }
                            items(state.activePipelines, key = { it.id }) { pipeline ->
                                ActivePipelineCard(
                                    pipeline = pipeline,
                                    onClick = { onPipelineClick(pipeline.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }

                        if (state.hasRecentResults) {
                            item { SectionHeader("Recent Results") }
                            items(state.recentResults, key = { it.id }) { result ->
                                PipelineResultRow(result)
                            }
                        }

                        if (!state.hasActivePipelines && !state.hasRecentResults && !state.isLoading) {
                            item { HomeEmptyState() }
                        }
                    }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun HomeEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No pipelines yet.", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Start a solve to see activity here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
