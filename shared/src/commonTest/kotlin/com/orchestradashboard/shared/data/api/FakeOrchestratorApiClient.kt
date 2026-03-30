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
import kotlinx.coroutines.flow.asFlow

class FakeOrchestratorApiClient : OrchestratorApi {
    var errorToThrow: Exception? = null

    var statusResult: SystemStatusDto? = null
    var projectsResult: List<ProjectDto> = emptyList()
    var projectDetailResult: ProjectDetailDto? = null
    var projectIssuesResult: List<OrchestratorIssueDto> = emptyList()
    var pipelinesResult: List<OrchestratorPipelineDto> = emptyList()
    var pipelineResult: OrchestratorPipelineDto? = null
    var checkpointsResult: List<CheckpointDto> = emptyList()
    var pipelineHistoryResult: List<PipelineHistoryDto> = emptyList()
    var eventsResult: List<PipelineEventDto> = emptyList()

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
    var getPipelineHistoryCallCount = 0
        private set
    var connectEventsCallCount = 0
        private set

    private fun maybeThrow() {
        errorToThrow?.let { throw it }
    }

    override suspend fun getStatus(): SystemStatusDto {
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

    override suspend fun getProjectIssues(name: String): List<OrchestratorIssueDto> {
        getProjectIssuesCallCount++
        maybeThrow()
        return projectIssuesResult
    }

    override suspend fun getPipelines(): List<OrchestratorPipelineDto> {
        getPipelinesCallCount++
        maybeThrow()
        return pipelinesResult
    }

    override suspend fun getPipeline(id: String): OrchestratorPipelineDto {
        getPipelineCallCount++
        maybeThrow()
        return pipelineResult ?: throw OrchestratorNotFoundException("Pipeline $id not found")
    }

    override suspend fun getCheckpoints(): List<CheckpointDto> {
        getCheckpointsCallCount++
        maybeThrow()
        return checkpointsResult
    }

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> {
        getPipelineHistoryCallCount++
        maybeThrow()
        return pipelineHistoryResult
    }

    override fun connectEvents(): Flow<PipelineEventDto> {
        connectEventsCallCount++
        maybeThrow()
        return eventsResult.asFlow()
    }
}
