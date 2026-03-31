package com.orchestradashboard.shared.data.api

import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
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

    suspend fun getPipelineHistory(): List<PipelineHistoryDto>

    fun connectEvents(): Flow<PipelineEventDto>
}
