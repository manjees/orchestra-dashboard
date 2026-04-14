package com.orchestradashboard.shared.domain.model

enum class OrchestratorCommandType { INIT, PLAN, DISCUSS, DESIGN, SHELL }

enum class ProjectVisibility { PUBLIC, PRIVATE }

data class InitProjectRequest(
    val name: String,
    val description: String,
    val visibility: ProjectVisibility,
)

data class CommandResult(
    val success: Boolean,
    val message: String,
    val pipelineId: String? = null,
)

data class PlannedIssue(
    val title: String,
    val body: String,
    val labels: List<String>,
)

data class PlanIssuesResult(
    val issues: List<PlannedIssue>,
)

data class DiscussResult(
    val answer: String,
    val suggestedIssues: List<PlannedIssue> = emptyList(),
)

data class DesignResult(
    val spec: String,
    val suggestedIssues: List<PlannedIssue> = emptyList(),
)

data class ShellResult(
    val output: String,
    val exitCode: Int,
)
