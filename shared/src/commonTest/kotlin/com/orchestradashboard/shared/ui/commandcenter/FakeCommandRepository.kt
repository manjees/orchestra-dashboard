package com.orchestradashboard.shared.ui.commandcenter

import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.domain.model.ShellResult
import com.orchestradashboard.shared.domain.repository.CommandRepository

class FakeCommandRepository : CommandRepository {
    var initProjectResult: Result<CommandResult> =
        Result.success(
            CommandResult(success = true, message = "Project initialized"),
        )
    var planIssuesResult: Result<PlanIssuesResult> = Result.success(PlanIssuesResult(emptyList()))
    var discussResult: Result<DiscussResult> = Result.success(DiscussResult(answer = "Answer"))
    var designResult: Result<DesignResult> = Result.success(DesignResult(spec = "Spec"))
    var shellResult: Result<ShellResult> = Result.success(ShellResult(output = "output", exitCode = 0))

    var initProjectCallCount = 0
        private set
    var planIssuesCallCount = 0
        private set
    var discussCallCount = 0
        private set
    var designCallCount = 0
        private set
    var executeShellCallCount = 0
        private set

    var lastInitRequest: InitProjectRequest? = null
        private set
    var lastPlanProject: String? = null
        private set
    var lastDiscussProject: String? = null
        private set
    var lastDiscussQuestion: String? = null
        private set
    var lastDesignProject: String? = null
        private set
    var lastDesignFigmaUrl: String? = null
        private set
    var lastShellCommand: String? = null
        private set

    override suspend fun initProject(request: InitProjectRequest): Result<CommandResult> {
        initProjectCallCount++
        lastInitRequest = request
        return initProjectResult
    }

    override suspend fun planIssues(projectName: String): Result<PlanIssuesResult> {
        planIssuesCallCount++
        lastPlanProject = projectName
        return planIssuesResult
    }

    override suspend fun discuss(
        projectName: String,
        question: String,
    ): Result<DiscussResult> {
        discussCallCount++
        lastDiscussProject = projectName
        lastDiscussQuestion = question
        return discussResult
    }

    override suspend fun design(
        projectName: String,
        figmaUrl: String,
    ): Result<DesignResult> {
        designCallCount++
        lastDesignProject = projectName
        lastDesignFigmaUrl = figmaUrl
        return designResult
    }

    override suspend fun executeShell(command: String): Result<ShellResult> {
        executeShellCallCount++
        lastShellCommand = command
        return shellResult
    }
}
