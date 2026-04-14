package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveResponse

data class ProjectExplorerUiState(
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val issues: List<Issue> = emptyList(),
    val checkpoints: List<Checkpoint> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingIssues: Boolean = false,
    val error: String? = null,
    val issuesPage: Int = 0,
    val issuesPageSize: Int = 20,
    val hasMoreIssues: Boolean = false,
    val retryingCheckpointId: String? = null,
    val retryResult: Result<Unit>? = null,
    // Solve Dialog state
    val showSolveDialog: Boolean = false,
    val selectedIssues: Set<Int> = emptySet(),
    val solveMode: SolveMode = SolveMode.AUTO,
    val isParallel: Boolean = false,
    val isSolving: Boolean = false,
    val solveResult: SolveResponse? = null,
    val solveError: String? = null,
)
