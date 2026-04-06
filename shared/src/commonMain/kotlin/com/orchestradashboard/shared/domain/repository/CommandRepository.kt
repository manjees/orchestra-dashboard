package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.domain.model.ShellResult

interface CommandRepository {
    suspend fun initProject(request: InitProjectRequest): Result<CommandResult>
    suspend fun planIssues(projectName: String): Result<PlanIssuesResult>
    suspend fun discuss(projectName: String, question: String): Result<DiscussResult>
    suspend fun design(projectName: String, figmaUrl: String): Result<DesignResult>
    suspend fun executeShell(command: String): Result<ShellResult>
}
