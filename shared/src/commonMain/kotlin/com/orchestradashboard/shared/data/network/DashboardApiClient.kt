package com.orchestradashboard.shared.data.network

import com.orchestradashboard.shared.data.dto.AgentCommandDto
import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.AgentPageDto
import com.orchestradashboard.shared.data.dto.AggregatedMetricDto
import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.CreateCommandDto
import com.orchestradashboard.shared.data.dto.LoginRequestDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.PipelineRunPageDto
import com.orchestradashboard.shared.data.dto.RefreshRequestDto
import com.orchestradashboard.shared.data.dto.analytics.AnalyticsSummaryDto
import com.orchestradashboard.shared.data.dto.analytics.DurationTrendDto
import com.orchestradashboard.shared.data.dto.analytics.StepFailureDto
import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DashboardApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val pollingIntervalMs: Long = 5000L,
) : DashboardApi {
    override fun observeAgents(): Flow<List<AgentDto>> =
        flow {
            while (true) {
                emit(getAgents())
                delay(pollingIntervalMs)
            }
        }

    override suspend fun getAgents(): List<AgentDto> {
        return httpClient.get("$baseUrl/api/v1/agents").body()
    }

    override suspend fun getAgent(agentId: String): AgentDto {
        return httpClient.get("$baseUrl/api/v1/agents/$agentId").body()
    }

    override suspend fun getPipelineRuns(agentId: String?): List<PipelineRunDto> {
        val page: PipelineRunPageDto =
            httpClient.get("$baseUrl/api/v1/pipelines") {
                if (agentId != null) parameter("agentId", agentId)
            }.body()
        return page.content
    }

    override suspend fun getPipelineRun(runId: String): PipelineRunDto {
        return httpClient.get("$baseUrl/api/v1/pipelines/$runId").body()
    }

    override suspend fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventDto> {
        return httpClient.get("$baseUrl/api/v1/events") {
            if (agentId != null) parameter("agentId", agentId)
            parameter("limit", limit)
        }.body()
    }

    override fun observePipelineRuns(agentId: String?): Flow<List<PipelineRunDto>> =
        flow {
            while (true) {
                emit(getPipelineRuns(agentId))
                delay(pollingIntervalMs)
            }
        }

    override fun observeEvents(agentId: String): Flow<List<AgentEventDto>> =
        flow {
            while (true) {
                emit(getRecentEvents(agentId = agentId))
                delay(pollingIntervalMs)
            }
        }

    override suspend fun registerAgent(agent: AgentDto): AgentDto {
        return httpClient.post("$baseUrl/api/v1/agents") {
            contentType(ContentType.Application.Json)
            setBody(agent)
        }.body()
    }

    override suspend fun deregisterAgent(agentId: String) {
        httpClient.delete("$baseUrl/api/v1/agents/$agentId")
    }

    override suspend fun getAgentsPaged(
        page: Int,
        pageSize: Int,
        status: String?,
    ): AgentPageDto {
        return httpClient.get("$baseUrl/api/v1/agents/paged") {
            parameter("page", page)
            parameter("pageSize", pageSize)
            if (status != null) parameter("status", status)
        }.body()
    }

    override suspend fun login(apiKey: String): AuthResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(apiKey))
        }.body()
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequestDto(refreshToken))
        }.body()
    }

    override suspend fun sendCommand(
        agentId: String,
        commandType: String,
    ): AgentCommandDto {
        return httpClient.post("$baseUrl/api/v1/commands") {
            contentType(ContentType.Application.Json)
            setBody(CreateCommandDto(agentId = agentId, commandType = commandType))
        }.body()
    }

    override suspend fun getCommands(
        agentId: String,
        limit: Int,
    ): List<AgentCommandDto> {
        return httpClient.get("$baseUrl/api/v1/commands") {
            parameter("agentId", agentId)
            parameter("limit", limit)
        }.body()
    }

    override suspend fun getAggregatedMetrics(
        agentId: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricDto> {
        return httpClient.get("$baseUrl/api/v1/metrics/$agentId/aggregate") {
            parameter("startTime", startTime)
            parameter("endTime", endTime)
        }.body()
    }

    // ─── BFF Proxy: Orchestrator ────────────────────────────────

    override suspend fun getProjects(): List<ProjectDto> = httpClient.get("$baseUrl/api/v1/projects").body()

    override suspend fun getProject(name: String): ProjectDetailDto = httpClient.get("$baseUrl/api/v1/projects/$name").body()

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): List<OrchestratorIssueDto> =
        httpClient.get("$baseUrl/api/v1/projects/$name/issues") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()

    override suspend fun getSystemStatus(): SystemStatusDto = httpClient.get("$baseUrl/api/v1/system/status").body()

    override suspend fun getActivePipelines(): List<OrchestratorPipelineDto> =
        httpClient.get("$baseUrl/api/v1/orchestrator/pipelines").body()

    override suspend fun getActivePipeline(id: String): OrchestratorPipelineDto =
        httpClient.get("$baseUrl/api/v1/orchestrator/pipelines/$id").body()

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> =
        httpClient.get("$baseUrl/api/v1/orchestrator/pipelines/history").body()

    override suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto =
        httpClient.get("$baseUrl/api/v1/orchestrator/pipelines/$parentId/parallel").body()

    override suspend fun getCheckpoints(): List<CheckpointDto> = httpClient.get("$baseUrl/api/v1/checkpoints").body()

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto =
        httpClient.post("$baseUrl/api/v1/checkpoints/$checkpointId/retry").body()

    override suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto =
        httpClient.post("$baseUrl/api/v1/commands/solve") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto =
        httpClient.post("$baseUrl/api/v1/commands/init") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto =
        httpClient.post("$baseUrl/api/v1/commands/plan") {
            contentType(ContentType.Application.Json)
            setBody(PlanIssuesRequestDto(project = projectName))
        }.body()

    override suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto =
        httpClient.post("$baseUrl/api/v1/commands/discuss") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun postDesign(request: DesignRequestDto): DesignResponseDto =
        httpClient.post("$baseUrl/api/v1/commands/design") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun postShell(request: ShellRequestDto): ShellResponseDto =
        httpClient.post("$baseUrl/api/v1/commands/shell") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    override suspend fun respondToApproval(
        approvalId: String,
        request: ApprovalRequestDto,
    ) {
        httpClient.post("$baseUrl/api/v1/approvals/$approvalId/respond") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    // ─── BFF: Analytics ─────────────────────────────────────────

    override suspend fun getAnalyticsSummary(
        project: String,
        from: Long?,
        to: Long?,
    ): AnalyticsSummaryDto =
        httpClient.get("$baseUrl/api/v1/analytics/pipelines/summary") {
            parameter("project", project)
            if (from != null) parameter("from", from)
            if (to != null) parameter("to", to)
        }.body()

    override suspend fun getStepFailures(project: String): List<StepFailureDto> =
        httpClient.get("$baseUrl/api/v1/analytics/pipelines/step-failures") {
            parameter("project", project)
        }.body()

    override suspend fun getDurationTrends(
        project: String,
        granularity: String,
    ): List<DurationTrendDto> =
        httpClient.get("$baseUrl/api/v1/analytics/pipelines/duration-trends") {
            parameter("project", project)
            parameter("granularity", granularity)
        }.body()
}
