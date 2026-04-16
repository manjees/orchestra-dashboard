package com.orchestradashboard.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.ui.component.CheckpointList
import com.orchestradashboard.shared.ui.component.ErrorBanner
import com.orchestradashboard.shared.ui.component.IssueRow
import com.orchestradashboard.shared.ui.component.LoadingOverlay
import com.orchestradashboard.shared.ui.component.ProjectCard
import com.orchestradashboard.shared.ui.component.SolveDialog
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel
import com.orchestradashboard.shared.ui.solvedialog.SolveDialogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectExplorerScreen(
    viewModel: ProjectExplorerViewModel,
    solveDialogViewModel: SolveDialogViewModel,
    onBackClick: () -> Unit,
    onNavigateToPipeline: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val solveDialogState by solveDialogViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadInitialData() }

    LaunchedEffect(solveDialogState.result) {
        solveDialogState.result?.let {
            onNavigateToPipeline(it.pipelineId)
            solveDialogViewModel.consumeResult()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Project Explorer") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        if (solveDialogState.showDialog) {
            SolveDialog(
                issues = uiState.issues,
                selectedIssues = solveDialogState.selectedIssues,
                solveMode = solveDialogState.mode,
                isParallel = solveDialogState.isParallel,
                isSolving = solveDialogState.isLoading,
                solveError = solveDialogState.error,
                onToggleIssue = { solveDialogViewModel.toggleIssueSelection(it) },
                onModeChange = { solveDialogViewModel.setMode(it) },
                onToggleParallel = { solveDialogViewModel.toggleParallel() },
                onSolve = { solveDialogViewModel.executeSolve() },
                onDismiss = { solveDialogViewModel.close() },
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                uiState.error?.let { error ->
                    ErrorBanner(message = error, onDismiss = { viewModel.clearError() })
                }

                when {
                    uiState.isLoading -> LoadingOverlay()
                    uiState.projects.isEmpty() -> ProjectEmptyState()
                    else -> {
                        ProjectGrid(
                            projects = uiState.projects,
                            selectedProject = uiState.selectedProject,
                            onProjectClick = { viewModel.selectProject(it) },
                            modifier = Modifier.weight(0.4f),
                        )

                        HorizontalDivider()

                        val issuesLazyListState = rememberLazyListState()
                        val shouldLoadMoreIssues by remember {
                            derivedStateOf {
                                val lastVisible = issuesLazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                                lastVisible != null &&
                                    uiState.issues.isNotEmpty() &&
                                    lastVisible >= uiState.issues.size - 1
                            }
                        }
                        LaunchedEffect(shouldLoadMoreIssues) {
                            if (shouldLoadMoreIssues) viewModel.loadMoreIssues()
                        }

                        LazyColumn(
                            state = issuesLazyListState,
                            modifier = Modifier.weight(0.6f).fillMaxWidth(),
                        ) {
                            // Issues section
                            if (uiState.selectedProject != null) {
                                item {
                                    Text(
                                        text = "Issues — ${uiState.selectedProject!!.name}",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                                if (uiState.isLoadingIssues && uiState.issues.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                } else if (uiState.issues.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                "No open issues",
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                } else {
                                    items(uiState.issues, key = { it.number }) { issue ->
                                        IssueRow(
                                            issue = issue,
                                            onSolveClick = {
                                                uiState.selectedProject?.let { project ->
                                                    solveDialogViewModel.open(project, issue)
                                                }
                                            },
                                        )
                                    }
                                }
                            }

                            // Checkpoints section
                            item {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Text(
                                    text = "Checkpoints",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                            if (uiState.checkpoints.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "No checkpoints",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            } else {
                                // Using specialized CheckpointList for rich UI and Retry integration
                                item {
                                    // Fix height in Nested LazyColumn
                                    CheckpointList(
                                        checkpoints = uiState.checkpoints,
                                        retryingCheckpointId = uiState.retryingCheckpointId,
                                        onRetryClick = { viewModel.retryCheckpoint(it) },
                                        modifier = Modifier.fillMaxWidth().height(400.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectGrid(
    projects: List<Project>,
    selectedProject: Project?,
    onProjectClick: (Project) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
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
            items(projects, key = { it.name }) { project ->
                ProjectCard(
                    project = project,
                    isSelected = project.name == selectedProject?.name,
                    onClick = { onProjectClick(project) },
                )
            }
        }
    }
}

@Composable
private fun ProjectEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No projects registered.", style = MaterialTheme.typography.bodyLarge)
    }
}
