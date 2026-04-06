package com.orchestradashboard.shared.ui.commandcenter

import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.model.PlannedIssue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.ProjectVisibility
import com.orchestradashboard.shared.domain.usecase.DesignUseCase
import com.orchestradashboard.shared.domain.usecase.DiscussUseCase
import com.orchestradashboard.shared.domain.usecase.ExecuteShellUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
import com.orchestradashboard.shared.domain.usecase.InitProjectUseCase
import com.orchestradashboard.shared.domain.usecase.PlanIssuesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommandCenterViewModel(
    private val initProjectUseCase: InitProjectUseCase,
    private val planIssuesUseCase: PlanIssuesUseCase,
    private val discussUseCase: DiscussUseCase,
    private val designUseCase: DesignUseCase,
    private val executeShellUseCase: ExecuteShellUseCase,
    private val getProjectsUseCase: GetProjectsUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(CommandCenterUiState())
    val uiState: StateFlow<CommandCenterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProjects = true) }
            getProjectsUseCase().fold(
                onSuccess = { projects ->
                    _uiState.update { it.copy(isLoadingProjects = false, projects = projects) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoadingProjects = false, error = e.message) }
                },
            )
        }
    }

    // ─── Tab ────────────────────────────────────────────────────

    fun selectTab(tab: CommandTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // ─── Init Project inputs ─────────────────────────────────────

    fun updateInitName(name: String) {
        _uiState.update { it.copy(initName = name) }
    }

    fun updateInitDescription(desc: String) {
        _uiState.update { it.copy(initDescription = desc) }
    }

    fun updateInitVisibility(vis: ProjectVisibility) {
        _uiState.update { it.copy(initVisibility = vis) }
    }

    // ─── Plan inputs ─────────────────────────────────────────────

    fun selectPlanProject(project: Project) {
        _uiState.update { it.copy(planSelectedProject = project) }
    }

    // ─── Discuss inputs ──────────────────────────────────────────

    fun selectDiscussProject(project: Project) {
        _uiState.update { it.copy(discussSelectedProject = project) }
    }

    fun updateDiscussQuestion(q: String) {
        _uiState.update { it.copy(discussQuestion = q) }
    }

    // ─── Design inputs ───────────────────────────────────────────

    fun selectDesignProject(project: Project) {
        _uiState.update { it.copy(designSelectedProject = project) }
    }

    fun updateDesignFigmaUrl(url: String) {
        _uiState.update { it.copy(designFigmaUrl = url) }
    }

    // ─── Shell inputs ────────────────────────────────────────────

    fun updateShellCommand(cmd: String) {
        _uiState.update { it.copy(shellCommand = cmd) }
    }

    // ─── Actions ─────────────────────────────────────────────────

    fun executeInit() {
        val state = _uiState.value
        if (state.initName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isInitLoading = true, error = null) }
            val request = InitProjectRequest(
                name = state.initName,
                description = state.initDescription,
                visibility = state.initVisibility,
            )
            initProjectUseCase(request).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isInitLoading = false, initResult = result) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isInitLoading = false, error = e.message) }
                },
            )
        }
    }

    fun executePlan() {
        val project = _uiState.value.planSelectedProject ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPlanLoading = true, error = null) }
            planIssuesUseCase(project.name).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isPlanLoading = false, planResult = result) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isPlanLoading = false, error = e.message) }
                },
            )
        }
    }

    fun executeDiscuss() {
        val state = _uiState.value
        val project = state.discussSelectedProject ?: return
        if (state.discussQuestion.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDiscussLoading = true, error = null) }
            discussUseCase(project.name, state.discussQuestion).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isDiscussLoading = false, discussResult = result) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isDiscussLoading = false, error = e.message) }
                },
            )
        }
    }

    fun executeDesign() {
        val state = _uiState.value
        val project = state.designSelectedProject ?: return
        if (state.designFigmaUrl.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDesignLoading = true, error = null) }
            designUseCase(project.name, state.designFigmaUrl).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isDesignLoading = false, designResult = result) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isDesignLoading = false, error = e.message) }
                },
            )
        }
    }

    fun executeShell() {
        val command = _uiState.value.shellCommand
        if (command.isBlank()) return

        if (ShellCommandConstants.dangerousPatterns.any { command.contains(it, ignoreCase = true) }) {
            _uiState.update { it.copy(showDangerDialog = true, pendingDangerousCommand = command) }
            return
        }

        runShellCommand(command)
    }

    fun confirmDangerousCommand() {
        val command = _uiState.value.pendingDangerousCommand ?: return
        _uiState.update { it.copy(showDangerDialog = false, pendingDangerousCommand = null) }
        runShellCommand(command)
    }

    fun cancelDangerousCommand() {
        _uiState.update { it.copy(showDangerDialog = false, pendingDangerousCommand = null) }
    }

    private fun runShellCommand(command: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isShellLoading = true, error = null) }
            executeShellUseCase(command).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isShellLoading = false, shellResult = result) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isShellLoading = false, error = e.message) }
                },
            )
        }
    }

    fun convertSuggestedIssue(issue: PlannedIssue) {
        _uiState.update { it.copy(pendingIssueConversion = issue) }
    }

    fun clearPendingIssueConversion() {
        _uiState.update { it.copy(pendingIssueConversion = null) }
    }

    // ─── Common ──────────────────────────────────────────────────

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
