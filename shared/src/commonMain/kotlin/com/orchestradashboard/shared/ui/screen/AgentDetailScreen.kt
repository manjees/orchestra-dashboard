package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.domain.model.AgentDetailViewModel
import com.orchestradashboard.shared.ui.component.AgentOverviewPanel
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.EventFeed
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.PipelineRunList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailScreen(
    viewModel: AgentDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabTitles = listOf("Overview", "Pipelines", "Events")

    LaunchedEffect(Unit) { viewModel.startObserving() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.agent?.name ?: "Agent Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            uiState.error?.let { ErrorBanner(message = it, onDismiss = { viewModel.clearError() }) }
            if (uiState.isLoading) {
                LoadingOverlay()
            } else {
                TabRow(selectedTabIndex = uiState.selectedTabIndex) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTabIndex == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(title) },
                        )
                    }
                }
                when (uiState.selectedTabIndex) {
                    0 -> uiState.agent?.let { AgentOverviewPanel(it, Modifier.fillMaxSize()) }
                    1 ->
                        PipelineRunList(
                            runs = uiState.pipelineRuns,
                            expandedIds = uiState.expandedPipelineIds,
                            onToggleExpand = { viewModel.togglePipelineExpanded(it) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    2 -> EventFeed(uiState.events, Modifier.fillMaxSize())
                }
            }
        }
    }
}
