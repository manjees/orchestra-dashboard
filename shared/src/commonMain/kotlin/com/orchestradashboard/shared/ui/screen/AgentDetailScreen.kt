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
import androidx.compose.ui.Modifier
import com.orchestradashboard.shared.domain.model.AgentDetailUiState
import com.orchestradashboard.shared.domain.model.DetailTab
import com.orchestradashboard.shared.ui.component.AgentOverviewPanel
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.EventFeed
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.PipelineRunList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailScreen(
    uiState: AgentDetailUiState,
    onTabSelected: (DetailTab) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = DetailTab.entries
    val tabLabels =
        mapOf(
            DetailTab.OVERVIEW to "Overview",
            DetailTab.PIPELINES to "Pipelines",
            DetailTab.EVENTS to "Events",
        )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.agent?.name ?: "Agent Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            uiState.error?.let { error ->
                ErrorBanner(message = error, onDismiss = {})
            }

            TabRow(selectedTabIndex = tabs.indexOf(uiState.selectedTab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        text = { Text(tabLabels[tab] ?: tab.name) },
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingOverlay()
                else ->
                    when (uiState.selectedTab) {
                        DetailTab.OVERVIEW -> {
                            uiState.agent?.let { agent ->
                                AgentOverviewPanel(agent = agent)
                            }
                        }
                        DetailTab.PIPELINES -> PipelineRunList(pipelineRuns = uiState.pipelineRuns)
                        DetailTab.EVENTS -> EventFeed(events = uiState.events)
                    }
            }
        }
    }
}
