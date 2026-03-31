package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import kotlinx.coroutines.flow.Flow

interface PipelineMonitorRepository {
    suspend fun getPipelineDetail(pipelineId: String): Result<MonitoredPipeline>

    fun observePipelineEvents(pipelineId: String): Flow<PipelineEventDto>
}
