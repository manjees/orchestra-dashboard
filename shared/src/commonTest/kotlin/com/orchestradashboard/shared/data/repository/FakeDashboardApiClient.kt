package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentCommandDto
import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.AgentPageDto
import com.orchestradashboard.shared.data.dto.AggregatedMetricDto
import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto
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
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.data.network.DashboardApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeDashboardApiClient(
    private val pollingIntervalMs: Long = 5000L,
) : DashboardApi {
    var agents: List<AgentDto> = emptyList()
    var pagedAgents: AgentPageDto = AgentPageDto(emptyList(), 0, 20, 0, 0)
    var pipelineRuns: List<PipelineRunDto> = emptyList()
    var events: List<AgentEventDto> = emptyList()
    var shouldFail: Boolean = false

    override fun observeAgents(): Flow<List<AgentDto>> =
        flow {
            while (true) {
                if (shouldFail) throw RuntimeException("Network error")
                emit(agents)
                delay(pollingIntervalMs)
            }
        }

    override suspend fun getAgents(): List<AgentDto> {
        if (shouldFail) throw RuntimeException("Network error")
        return agents
    }

    override suspend fun getAgent(agentId: String): AgentDto {
        if (shouldFail) throw RuntimeException("Network error")
        return agents.first { it.id == agentId }
    }

    override suspend fun getPipelineRuns(agentId: String?): List<PipelineRunDto> {
        if (shouldFail) throw RuntimeException("Network error")
        return if (agentId != null) {
            pipelineRuns.filter { it.agentId == agentId }
        } else {
            pipelineRuns
        }
    }

    override suspend fun getPipelineRun(id: String): PipelineRunDto {
        if (shouldFail) throw RuntimeException("Network error")
        return pipelineRuns.first { it.id == id }
    }

    override suspend fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventDto> {
        if (shouldFail) throw RuntimeException("Network error")
        val filtered =
            if (agentId != null) {
                events.filter { it.agentId == agentId }
            } else {
                events
            }
        return filtered.take(limit)
    }

    override fun observePipelineRuns(agentId: String?): Flow<List<PipelineRunDto>> =
        flow {
            while (true) {
                if (shouldFail) throw RuntimeException("Network error")
                emit(getPipelineRuns(agentId))
                delay(pollingIntervalMs)
            }
        }

    override fun observeEvents(agentId: String): Flow<List<AgentEventDto>> =
        flow {
            while (true) {
                if (shouldFail) throw RuntimeException("Network error")
                emit(getRecentEvents(agentId = agentId))
                delay(pollingIntervalMs)
            }
        }

    override suspend fun registerAgent(agent: AgentDto): AgentDto {
        if (shouldFail) throw RuntimeException("Network error")
        return agent
    }

    override suspend fun deregisterAgent(agentId: String) {
        if (shouldFail) throw RuntimeException("Network error")
    }

    override suspend fun getAgentsPaged(
        page: Int,
        pageSize: Int,
        status: String?,
    ): AgentPageDto {
        if (shouldFail) throw RuntimeException("Network error")
        return pagedAgents
    }

    override suspend fun login(apiKey: String): AuthResponseDto {
        if (shouldFail) throw RuntimeException("Network error")
        return AuthResponseDto(accessToken = "fake-access", refreshToken = "fake-refresh", expiresIn = 900)
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        if (shouldFail) throw RuntimeException("Network error")
        return AuthResponseDto(accessToken = "fake-access", refreshToken = "fake-refresh", expiresIn = 900)
    }

    override suspend fun sendCommand(
        agentId: String,
        commandType: String,
    ): AgentCommandDto {
        if (shouldFail) throw RuntimeException("Network error")
        return AgentCommandDto(
            id = "cmd-1",
            agentId = agentId,
            commandType = commandType,
            status = "PENDING",
            requestedAt = 1000L,
            requestedBy = "test-user",
        )
    }

    override suspend fun getCommands(
        agentId: String,
        limit: Int,
    ): List<AgentCommandDto> {
        if (shouldFail) throw RuntimeException("Network error")
        return emptyList()
    }

    override suspend fun getAggregatedMetrics(
        agentId: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricDto> {
        if (shouldFail) throw RuntimeException("Network error")
        return emptyList()
    }

    // ─── BFF Proxy stubs ─────────────────────────────���──────────

    override suspend fun getProjects(): List<ProjectDto> = throw NotImplementedError()

    override suspend fun getProject(name: String): ProjectDetailDto = throw NotImplementedError()

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): List<OrchestratorIssueDto> = throw NotImplementedError()

    override suspend fun getSystemStatus(): SystemStatusDto = throw NotImplementedError()

    override suspend fun getActivePipelines(): List<OrchestratorPipelineDto> = throw NotImplementedError()

    override suspend fun getActivePipeline(id: String): OrchestratorPipelineDto = throw NotImplementedError()

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> = throw NotImplementedError()

    override suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto = throw NotImplementedError()

    override suspend fun getCheckpoints(): List<CheckpointDto> = throw NotImplementedError()

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto = throw NotImplementedError()

    override suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto = throw NotImplementedError()

    override suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto = throw NotImplementedError()

    override suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto = throw NotImplementedError()

    override suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto = throw NotImplementedError()

    override suspend fun postDesign(request: DesignRequestDto): DesignResponseDto = throw NotImplementedError()

    override suspend fun postShell(request: ShellRequestDto): ShellResponseDto = throw NotImplementedError()

    override suspend fun respondToApproval(
        approvalId: String,
        request: ApprovalRequestDto,
    ) = throw NotImplementedError()

    // ─── Analytics stubs ───────���────────────────��───────────────

    override suspend fun getAnalyticsSummary(
        project: String,
        from: Long?,
        to: Long?,
    ): AnalyticsSummaryDto = throw NotImplementedError()

    override suspend fun getStepFailures(project: String): List<StepFailureDto> = throw NotImplementedError()

    override suspend fun getDurationTrends(
        project: String,
        granularity: String,
    ): List<DurationTrendDto> = throw NotImplementedError()
}
