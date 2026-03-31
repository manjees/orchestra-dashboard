package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project

data class ProjectExplorerUiState(
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val issues: List<Issue> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingIssues: Boolean = false,
    val error: String? = null,
)
