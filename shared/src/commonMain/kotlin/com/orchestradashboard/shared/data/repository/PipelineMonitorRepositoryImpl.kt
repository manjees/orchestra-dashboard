package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorApi
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.mapper.MonitoredPipelineMapper
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.repository.PipelineMonitorRepository
import kotlinx.coroutines.flow.Flow

class PipelineMonitorRepositoryImpl(
    private val api: OrchestratorApi,
    private val mapper: MonitoredPipelineMapper,
) : PipelineMonitorRepository {
    override suspend fun getPipelineDetail(pipelineId: String): Result<MonitoredPipeline> =
        try {
            val dto = api.getPipeline(pipelineId)
            Result.success(mapper.mapToDomain(dto))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun observePipelineEvents(pipelineId: String): Flow<PipelineEventDto> = api.connectEvents(pipelineId)
}
