package com.orchestradashboard.shared.ui.solvedialog

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.usecase.ExecuteSolveUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SolveDialogViewModel(
    private val executeSolveUseCase: ExecuteSolveUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(SolveDialogState())
    val uiState: StateFlow<SolveDialogState> = _uiState.asStateFlow()

    fun open(
        project: Project,
        issue: Issue,
    ) {
        _uiState.update {
            SolveDialogState(
                showDialog = true,
                projectName = project.name,
                selectedIssues = setOf(issue.number),
                mode = SolveMode.AUTO,
                isParallel = false,
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

    fun setMode(mode: SolveMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun toggleParallel() {
        _uiState.update { it.copy(isParallel = !it.isParallel) }
    }

    fun executeSolve() {
        val state = _uiState.value
        val projectName = state.projectName ?: return
        if (state.selectedIssues.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val request =
                SolveRequest(
                    projectName = projectName,
                    issueNumbers = state.selectedIssues.toList(),
                    mode = state.mode,
                    parallel = state.isParallel,
                )
            executeSolveUseCase(request).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = response,
                            showDialog = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to execute solve",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeResult() {
        _uiState.update { it.copy(result = null) }
    }

    fun close() {
        _uiState.update { SolveDialogState() }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
