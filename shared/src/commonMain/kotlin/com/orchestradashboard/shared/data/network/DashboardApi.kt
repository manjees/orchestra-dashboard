package com.orchestradashboard.shared.data.network

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
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the Dashboard API surface consumed by repository implementations.
 */
interface DashboardApi {
    fun observeAgents(): Flow<List<AgentDto>>

    suspend fun getAgents(): List<AgentDto>

    suspend fun getAgent(agentId: String): AgentDto

    suspend fun getPipelineRuns(agentId: String? = null): List<PipelineRunDto>

    suspend fun getPipelineRun(runId: String): PipelineRunDto

    suspend fun getRecentEvents(
        agentId: String? = null,
        limit: Int = 50,
    ): List<AgentEventDto>

    fun observePipelineRuns(agentId: String? = null): Flow<List<PipelineRunDto>>

    fun observeEvents(agentId: String): Flow<List<AgentEventDto>>

    suspend fun registerAgent(agent: AgentDto): AgentDto

    suspend fun deregisterAgent(agentId: String)

    suspend fun getAgentsPaged(
        page: Int,
        pageSize: Int,
        status: String? = null,
    ): AgentPageDto

    suspend fun login(apiKey: String): AuthResponseDto

    suspend fun refreshToken(refreshToken: String): AuthResponseDto

    suspend fun sendCommand(
        agentId: String,
        commandType: String,
    ): AgentCommandDto

    suspend fun getCommands(
        agentId: String,
        limit: Int = 20,
    ): List<AgentCommandDto>

    suspend fun getAggregatedMetrics(
        agentId: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricDto>

    // ─── BFF Proxy: Orchestrator ────────────────────────────────

    suspend fun getProjects(): List<ProjectDto>

    suspend fun getProject(name: String): ProjectDetailDto

    suspend fun getProjectIssues(
        name: String,
        page: Int = 0,
        pageSize: Int = 20,
    ): List<OrchestratorIssueDto>

    suspend fun getSystemStatus(): SystemStatusDto

    suspend fun getActivePipelines(): List<OrchestratorPipelineDto>

    suspend fun getActivePipeline(id: String): OrchestratorPipelineDto

    suspend fun getPipelineHistory(): List<PipelineHistoryDto>

    suspend fun getPagedHistory(
        project: String? = null,
        status: String? = null,
        keyword: String? = null,
        hours: Int? = null,
        page: Int = 0,
        size: Int = 20,
    ): PipelineHistoryPageDto

    suspend fun getHistoryDetail(id: String): PipelineHistoryDetailDto

    suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto

    suspend fun getCheckpoints(): List<CheckpointDto>

    suspend fun retryCheckpoint(checkpointId: String): CheckpointDto

    suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto

    suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto

    suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto

    suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto

    suspend fun postDesign(request: DesignRequestDto): DesignResponseDto

    suspend fun postShell(request: ShellRequestDto): ShellResponseDto

    suspend fun respondToApproval(
        approvalId: String,
        request: ApprovalRequestDto,
    )

    // ─── BFF: Analytics ─────────────────────────────────────────

    suspend fun getAnalyticsSummary(
        project: String,
        from: Long? = null,
        to: Long? = null,
    ): AnalyticsSummaryDto

    suspend fun getStepFailures(project: String): List<StepFailureDto>

    suspend fun getDurationTrends(
        project: String,
        granularity: String = "day",
    ): List<DurationTrendDto>

    // ─── BFF: Notifications ─────────────────────────────────────

    suspend fun registerDeviceToken(request: DeviceTokenRequestDto): DeviceTokenResponseDto

    suspend fun unregisterDeviceToken(token: String)
}
