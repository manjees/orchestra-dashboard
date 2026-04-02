package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.ParallelPipelineGroup
import kotlinx.coroutines.flow.Flow

interface PipelineMonitorRepository {
    suspend fun getPipelineDetail(pipelineId: String): Result<MonitoredPipeline>

    suspend fun getParallelPipelines(parentId: String): Result<ParallelPipelineGroup>

    fun observePipelineEvents(pipelineId: String): Flow<PipelineEventDto>
}
