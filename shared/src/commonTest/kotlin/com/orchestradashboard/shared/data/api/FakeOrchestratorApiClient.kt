package com.orchestradashboard.shared.data.api

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
import com.orchestradashboard.shared.data.dto.notification.DeviceTokenRequestDto
import com.orchestradashboard.shared.data.dto.notification.DeviceTokenResponseDto
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
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryPageDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.data.network.DashboardApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Suppress("TooManyFunctions")
class FakeOrchestratorApiClient : DashboardApi {
    var errorToThrow: Exception? = null

    var initProjectResult: InitProjectResponseDto? = null
    var planIssuesResult: PlanIssuesResponseDto? = null
    var discussResult: DiscussResponseDto? = null
    var designResult: DesignResponseDto? = null
    var shellResult: ShellResponseDto? = null

    var postInitProjectCallCount = 0
        private set
    var postPlanIssuesCallCount = 0
        private set
    var postDiscussCallCount = 0
        private set
    var postDesignCallCount = 0
        private set
    var postShellCallCount = 0
        private set

    var statusResult: SystemStatusDto? = null
    var projectsResult: List<ProjectDto> = emptyList()
    var projectDetailResult: ProjectDetailDto? = null
    var projectIssuesResult: List<OrchestratorIssueDto> = emptyList()
    var pipelinesResult: List<OrchestratorPipelineDto> = emptyList()
    var pipelineResult: OrchestratorPipelineDto? = null
    var checkpointsResult: List<CheckpointDto> = emptyList()
    var retryCheckpointResult: CheckpointDto? = null
    var pipelineHistoryResult: List<PipelineHistoryDto> = emptyList()
    var eventsResult: List<PipelineEventDto> = emptyList()
    var parallelPipelinesResult: ParallelPipelineGroupDto? = null
    var postSolveResult: SolveCommandResponseDto? = null

    var getStatusCallCount = 0
        private set
    var getProjectsCallCount = 0
        private set
    var getProjectCallCount = 0
        private set
    var getProjectIssuesCallCount = 0
        private set
    var getPipelinesCallCount = 0
        private set
    var getPipelineCallCount = 0
        private set
    var getCheckpointsCallCount = 0
        private set
    var retryCheckpointCallCount = 0
        private set
    var lastRetriedCheckpointId: String? = null
        private set
    var getPipelineHistoryCallCount = 0
        private set
    var connectEventsCallCount = 0
        private set

    private fun maybeThrow() {
        errorToThrow?.let { throw it }
    }

    // ─── DashboardApi (BFF) methods ──────────────────────────────

    override fun observeAgents(): Flow<List<AgentDto>> = emptyFlow()

    override suspend fun getAgents(): List<AgentDto> = emptyList()

    override suspend fun getAgent(agentId: String): AgentDto = throw NotImplementedError()

    override suspend fun getPipelineRuns(agentId: String?): List<PipelineRunDto> = emptyList()

    override suspend fun getPipelineRun(runId: String): PipelineRunDto = throw NotImplementedError()

    override suspend fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventDto> = emptyList()

    override fun observePipelineRuns(agentId: String?): Flow<List<PipelineRunDto>> = emptyFlow()

    override fun observeEvents(agentId: String): Flow<List<AgentEventDto>> = emptyFlow()

    override suspend fun registerAgent(agent: AgentDto): AgentDto = throw NotImplementedError()

    override suspend fun deregisterAgent(agentId: String) = throw NotImplementedError()

    override suspend fun getAgentsPaged(
        page: Int,
        pageSize: Int,
        status: String?,
    ): AgentPageDto = throw NotImplementedError()

    override suspend fun login(apiKey: String): AuthResponseDto = throw NotImplementedError()

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto = throw NotImplementedError()

    override suspend fun sendCommand(
        agentId: String,
        commandType: String,
    ): AgentCommandDto = throw NotImplementedError()

    override suspend fun getCommands(
        agentId: String,
        limit: Int,
    ): List<AgentCommandDto> = emptyList()

    override suspend fun getAggregatedMetrics(
        agentId: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricDto> = emptyList()

    // ─── BFF Proxy: Orchestrator ────────────────────────────────

    override suspend fun getSystemStatus(): SystemStatusDto {
        getStatusCallCount++
        maybeThrow()
        return statusResult ?: throw OrchestratorNetworkException("No status result configured")
    }

    override suspend fun getProjects(): List<ProjectDto> {
        getProjectsCallCount++
        maybeThrow()
        return projectsResult
    }

    override suspend fun getProject(name: String): ProjectDetailDto {
        getProjectCallCount++
        maybeThrow()
        return projectDetailResult ?: throw OrchestratorNotFoundException("Project $name not found")
    }

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): List<OrchestratorIssueDto> {
        getProjectIssuesCallCount++
        maybeThrow()
        return projectIssuesResult
    }

    override suspend fun getActivePipelines(): List<OrchestratorPipelineDto> {
        getPipelinesCallCount++
        maybeThrow()
        return pipelinesResult
    }

    override suspend fun getActivePipeline(id: String): OrchestratorPipelineDto {
        getPipelineCallCount++
        maybeThrow()
        return pipelineResult ?: throw OrchestratorNotFoundException("Pipeline $id not found")
    }

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> {
        getPipelineHistoryCallCount++
        maybeThrow()
        return pipelineHistoryResult
    }

    override suspend fun getPagedHistory(
        project: String?,
        status: String?,
        keyword: String?,
        hours: Int?,
        page: Int,
        size: Int,
    ): PipelineHistoryPageDto {
        maybeThrow()
        return PipelineHistoryPageDto()
    }

    override suspend fun getHistoryDetail(id: String): PipelineHistoryDetailDto {
        maybeThrow()
        throw NotImplementedError("FakeOrchestratorApiClient does not implement getHistoryDetail")
    }

    override suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto {
        maybeThrow()
        return parallelPipelinesResult
            ?: throw OrchestratorNotFoundException("Parallel pipelines for $parentId not found")
    }

    override suspend fun getCheckpoints(): List<CheckpointDto> {
        getCheckpointsCallCount++
        maybeThrow()
        return checkpointsResult
    }

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto {
        retryCheckpointCallCount++
        lastRetriedCheckpointId = checkpointId
        maybeThrow()
        return retryCheckpointResult
            ?: throw OrchestratorNotFoundException("Checkpoint $checkpointId not found")
    }

    override suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto {
        maybeThrow()
        return postSolveResult ?: SolveCommandResponseDto(pipelineId = "pipe-fake", status = "started")
    }

    override suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto {
        postInitProjectCallCount++
        maybeThrow()
        return initProjectResult ?: throw OrchestratorNetworkException("No initProjectResult configured")
    }

    override suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto {
        postPlanIssuesCallCount++
        maybeThrow()
        return planIssuesResult ?: throw OrchestratorNetworkException("No planIssuesResult configured")
    }

    override suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto {
        postDiscussCallCount++
        maybeThrow()
        return discussResult ?: throw OrchestratorNetworkException("No discussResult configured")
    }

    override suspend fun postDesign(request: DesignRequestDto): DesignResponseDto {
        postDesignCallCount++
        maybeThrow()
        return designResult ?: throw OrchestratorNetworkException("No designResult configured")
    }

    override suspend fun postShell(request: ShellRequestDto): ShellResponseDto {
        postShellCallCount++
        maybeThrow()
        return shellResult ?: throw OrchestratorNetworkException("No shellResult configured")
    }

    override suspend fun respondToApproval(
        approvalId: String,
        request: ApprovalRequestDto,
    ) {
        maybeThrow()
    }

    // ─── BFF: Analytics ─────────────────────────────────────────

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

    // ─── Notifications stubs ────────────────────────────────────

    override suspend fun registerDeviceToken(request: DeviceTokenRequestDto): DeviceTokenResponseDto =
        DeviceTokenResponseDto(registeredAt = 0L)

    override suspend fun unregisterDeviceToken(token: String) {
        // no-op
    }
}
