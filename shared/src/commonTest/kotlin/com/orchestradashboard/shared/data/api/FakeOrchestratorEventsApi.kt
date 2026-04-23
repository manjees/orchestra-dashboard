package com.orchestradashboard.shared.data.api

import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.LogEntryDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * Minimal fake that only supports connectEvents() for WebSocket event testing.
 */
class FakeOrchestratorEventsApi : OrchestratorApi {
    var eventsResult: List<PipelineEventDto> = emptyList()

    override fun connectEvents(): Flow<PipelineEventDto> = eventsResult.asFlow()

    override fun connectEvents(pipelineId: String): Flow<PipelineEventDto> = eventsResult.filter { it.pipelineId == pipelineId }.asFlow()

    override fun connectLogStream(stepId: String): Flow<LogEntryDto> = throw NotImplementedError()

    override suspend fun getStatus(): SystemStatusDto = throw NotImplementedError()

    override suspend fun getProjects(): List<ProjectDto> = throw NotImplementedError()

    override suspend fun getProject(name: String): ProjectDetailDto = throw NotImplementedError()

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): List<OrchestratorIssueDto> = throw NotImplementedError()

    override suspend fun getPipelines(): List<OrchestratorPipelineDto> = throw NotImplementedError()

    override suspend fun getPipeline(id: String): OrchestratorPipelineDto = throw NotImplementedError()

    override suspend fun getCheckpoints(): List<CheckpointDto> = throw NotImplementedError()

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto = throw NotImplementedError()

    override suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto = throw NotImplementedError()

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> = throw NotImplementedError()

    override suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto = throw NotImplementedError()

    override suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto = throw NotImplementedError()

    override suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto = throw NotImplementedError()

    override suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto = throw NotImplementedError()

    override suspend fun postDesign(request: DesignRequestDto): DesignResponseDto = throw NotImplementedError()

    override suspend fun postShell(request: ShellRequestDto): ShellResponseDto = throw NotImplementedError()

    override suspend fun respondToApproval(
        approvalId: String,
        request: ApprovalRequestDto,
    ) = throw NotImplementedError()
}
