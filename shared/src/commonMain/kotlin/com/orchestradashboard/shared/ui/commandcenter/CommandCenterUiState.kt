package com.orchestradashboard.shared.ui.commandcenter

import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.domain.model.PlannedIssue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.ProjectVisibility
import com.orchestradashboard.shared.domain.model.ShellResult

enum class CommandTab { INIT, PLAN, DISCUSS, DESIGN, SHELL }

data class CommandCenterUiState(
    val activeTab: CommandTab = CommandTab.INIT,
    val projects: List<Project> = emptyList(),
    val isLoadingProjects: Boolean = false,
    // Init Project
    val initName: String = "",
    val initDescription: String = "",
    val initVisibility: ProjectVisibility = ProjectVisibility.PUBLIC,
    val isInitLoading: Boolean = false,
    val initResult: CommandResult? = null,
    // Plan Issues
    val planSelectedProject: Project? = null,
    val isPlanLoading: Boolean = false,
    val planResult: PlanIssuesResult? = null,
    // Discuss
    val discussSelectedProject: Project? = null,
    val discussQuestion: String = "",
    val isDiscussLoading: Boolean = false,
    val discussResult: DiscussResult? = null,
    // Design
    val designSelectedProject: Project? = null,
    val designFigmaUrl: String = "",
    val isDesignLoading: Boolean = false,
    val designResult: DesignResult? = null,
    // Shell
    val shellCommand: String = "",
    val isShellLoading: Boolean = false,
    val shellResult: ShellResult? = null,
    val showDangerDialog: Boolean = false,
    val pendingDangerousCommand: String? = null,
    // Common
    val error: String? = null,
    val pendingIssueConversion: PlannedIssue? = null,
)
