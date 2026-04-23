package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.HistoryDetailSheet
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.PipelineResultRow
import com.orchestradashboard.shared.ui.component.PipelineStatusFilterBar
import com.orchestradashboard.shared.ui.history.HistoryViewModel

private const val LOAD_MORE_THRESHOLD = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    onAnalyticsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    LaunchedEffect(listState, state.historyItems.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= state.historyItems.size - LOAD_MORE_THRESHOLD) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Analytics")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(Modifier.fillMaxSize()) {
                state.error?.let { error ->
                    ErrorBanner(message = error, onDismiss = { viewModel.clearError() })
                }

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search by issue title") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                )

                PipelineStatusFilterBar(
                    selected = state.filter.status,
                    onSelected = { status ->
                        viewModel.applyFilter(state.filter.copy(status = status))
                    },
                )

                HistoryTimeRangeRow(
                    selected = state.filter.timeRange,
                    onSelected = { viewModel.selectTimeRange(it) },
                )

                HistoryBody(
                    state = state,
                    listState = listState,
                    onRowClick = { id -> viewModel.selectHistory(id) },
                )
            }

            if (state.showDetail) {
                HistoryDetailOverlay(
                    state = state,
                    onDismiss = { viewModel.clearSelection() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTimeRangeRow(
    selected: TimeRange?,
    onSelected: (TimeRange?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text("Any time") },
        )
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = selected == range,
                onClick = { onSelected(range) },
                label = { Text(range.label) },
            )
        }
    }
}

@Composable
private fun HistoryBody(
    state: com.orchestradashboard.shared.ui.history.HistoryUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onRowClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> LoadingOverlay(modifier)
        !state.hasResults -> HistoryEmptyState(state.isFiltered, modifier)
        else ->
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
            ) {
                items(state.historyItems, key = { it.id }) { result ->
                    PipelineResultRow(
                        result = result,
                        onClick = { onRowClick(result.id) },
                    )
                }
                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
    }
}

@Composable
private fun HistoryEmptyState(
    isFiltered: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isFiltered) "No matching history." else "No pipeline history yet.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text =
                    if (isFiltered) {
                        "Try adjusting filters or clearing the search."
                    } else {
                        "Completed pipelines will appear here."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistoryDetailOverlay(
    state: com.orchestradashboard.shared.ui.history.HistoryUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        when {
            state.isLoadingDetail ->
                LoadingOverlay()
            state.detailError != null ->
                ErrorBanner(message = state.detailError, onDismiss = onDismiss)
            state.historyDetail != null ->
                HistoryDetailSheet(
                    detail = state.historyDetail,
                    onDismiss = onDismiss,
                )
        }
    }
}
