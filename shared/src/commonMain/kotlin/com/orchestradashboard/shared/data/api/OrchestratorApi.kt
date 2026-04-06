package com.orchestradashboard.shared.data.api

import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import kotlinx.coroutines.flow.Flow

interface OrchestratorApi {
    suspend fun getStatus(): SystemStatusDto

    suspend fun getProjects(): List<ProjectDto>

    suspend fun getProject(name: String): ProjectDetailDto

    suspend fun getProjectIssues(
        name: String,
        page: Int = 0,
        pageSize: Int = 20,
    ): List<OrchestratorIssueDto>

    suspend fun getPipelines(): List<OrchestratorPipelineDto>

    suspend fun getPipeline(id: String): OrchestratorPipelineDto

    suspend fun getCheckpoints(): List<CheckpointDto>

    suspend fun retryCheckpoint(checkpointId: String): CheckpointDto

    suspend fun postSolve(request: SolveCommandRequestDto): SolveCommandResponseDto

    suspend fun getPipelineHistory(): List<PipelineHistoryDto>

    suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto

    fun connectEvents(): Flow<PipelineEventDto>

    fun connectEvents(pipelineId: String): Flow<PipelineEventDto>
}
