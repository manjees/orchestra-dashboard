package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorApi
import com.orchestradashboard.shared.data.dto.orchestrator.DesignRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.mapper.CommandMapper
import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.domain.model.ShellResult
import com.orchestradashboard.shared.domain.repository.CommandRepository

class CommandRepositoryImpl(
    private val api: OrchestratorApi,
    private val mapper: CommandMapper,
) : CommandRepository {

    override suspend fun initProject(request: InitProjectRequest): Result<CommandResult> = runCatching {
        val dto = InitProjectRequestDto(
            name = request.name,
            description = request.description,
            visibility = request.visibility.name,
        )
        mapper.mapInitResponse(api.postInitProject(dto))
    }

    override suspend fun planIssues(projectName: String): Result<PlanIssuesResult> = runCatching {
        mapper.mapPlanResponse(api.postPlanIssues(projectName))
    }

    override suspend fun discuss(projectName: String, question: String): Result<DiscussResult> = runCatching {
        mapper.mapDiscussResponse(api.postDiscuss(DiscussRequestDto(project = projectName, question = question)))
    }

    override suspend fun design(projectName: String, figmaUrl: String): Result<DesignResult> = runCatching {
        mapper.mapDesignResponse(api.postDesign(DesignRequestDto(project = projectName, figmaUrl = figmaUrl)))
    }

    override suspend fun executeShell(command: String): Result<ShellResult> = runCatching {
        mapper.mapShellResponse(api.postShell(ShellRequestDto(command = command)))
    }
}
