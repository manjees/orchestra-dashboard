package com.orchestradashboard.shared.domain.model

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
    val issuesTotalCount: Int = 0,
)
