package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.usecase.ExecuteSolveUseCase
import com.orchestradashboard.shared.domain.usecase.GetCheckpointsUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectIssuesUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
import com.orchestradashboard.shared.domain.usecase.RetryCheckpointUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectExplorerViewModel(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getProjectIssuesUseCase: GetProjectIssuesUseCase,
    private val getCheckpointsUseCase: GetCheckpointsUseCase,
    private val retryCheckpointUseCase: RetryCheckpointUseCase,
    private val executeSolveUseCase: ExecuteSolveUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(ProjectExplorerUiState())
    private var issuesJob: Job? = null

    val uiState: StateFlow<ProjectExplorerUiState> = _uiState.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val projectsDeferred = async { getProjectsUseCase() }
            val checkpointsDeferred = async { getCheckpointsUseCase() }

            val projectsResult = projectsDeferred.await()
            val checkpointsResult = checkpointsDeferred.await()

            _uiState.update { state ->
                state.copy(
                    projects = projectsResult.getOrDefault(emptyList()),
                    checkpoints = checkpointsResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error =
                        projectsResult.exceptionOrNull()?.message
                            ?: checkpointsResult.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun selectProject(project: Project) {
        issuesJob?.cancel()
        _uiState.update {
            it.copy(
                selectedProject = project,
                issues = emptyList(),
                issuesPage = 0,
                hasMoreIssues = false,
                isLoadingIssues = true,
            )
        }
        loadIssuesPage(0)
    }

    fun loadMoreIssues() {
        val state = _uiState.value
        if (!state.hasMoreIssues || state.isLoadingIssues) return
        loadIssuesPage(state.issuesPage + 1)
    }

    private fun loadIssuesPage(page: Int) {
        val project = _uiState.value.selectedProject ?: return
        val pageSize = _uiState.value.issuesPageSize
        issuesJob?.cancel()
        issuesJob =
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingIssues = true) }
                getProjectIssuesUseCase(project.name, page, pageSize).fold(
                    onSuccess = { newIssues ->
                        _uiState.update { state ->
                            val allIssues = if (page == 0) newIssues else state.issues + newIssues
                            state.copy(
                                issues = allIssues,
                                issuesPage = page,
                                hasMoreIssues = newIssues.size == pageSize,
                                isLoadingIssues = false,
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                error = e.message ?: "Failed to load issues",
                                isLoadingIssues = false,
                            )
                        }
                    },
                )
            }
    }

    fun retryCheckpoint(checkpointId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(retryingCheckpointId = checkpointId, retryResult = null) }
            retryCheckpointUseCase(checkpointId).fold(
                onSuccess = { _ ->
                    _uiState.update { it.copy(retryingCheckpointId = null, retryResult = Result.success(Unit)) }
                    loadInitialData() // Refresh list to show running status
                },
                onFailure = { e ->
                    _uiState.update { it.copy(retryingCheckpointId = null, retryResult = Result.failure(e)) }
                },
            )
        }
    }

    fun refresh() {
        _uiState.update { ProjectExplorerUiState() }
        loadInitialData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun openSolveDialog(issue: Issue) {
        _uiState.update {
            it.copy(
                showSolveDialog = true,
                selectedIssues = setOf(issue.number),
                solveMode = SolveMode.AUTO,
                isParallel = false,
                solveError = null,
                solveResult = null,
            )
        }
    }

    fun toggleIssueSelection(issueNumber: Int) {
        _uiState.update { state ->
            val current = state.selectedIssues
            val updated = if (current.contains(issueNumber)) current - issueNumber else current + issueNumber
            state.copy(selectedIssues = updated)
        }
    }

    fun setSolveMode(mode: SolveMode) {
        _uiState.update { it.copy(solveMode = mode) }
    }

    fun toggleParallel() {
        _uiState.update { it.copy(isParallel = !it.isParallel) }
    }

    fun executeSolve() {
        val state = _uiState.value
        val project = state.selectedProject ?: return
        if (state.selectedIssues.isEmpty()) return

        _uiState.update { it.copy(isSolving = true, solveError = null) }

        viewModelScope.launch {
            val request =
                SolveRequest(
                    projectName = project.name,
                    issueNumbers = state.selectedIssues.toList(),
                    mode = state.solveMode,
                    parallel = state.isParallel,
                )
            executeSolveUseCase(request).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isSolving = false,
                            solveResult = response,
                            showSolveDialog = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSolving = false,
                            solveError = e.message ?: "Failed to execute solve",
                        )
                    }
                },
            )
        }
    }

    fun closeSolveDialog() {
        _uiState.update {
            it.copy(
                showSolveDialog = false,
                selectedIssues = emptySet(),
                solveMode = SolveMode.AUTO,
                isParallel = false,
                solveError = null,
                solveResult = null,
            )
        }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
