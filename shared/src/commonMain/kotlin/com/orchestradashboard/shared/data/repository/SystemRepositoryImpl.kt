package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorApi
import com.orchestradashboard.shared.data.mapper.ActivePipelineMapper
import com.orchestradashboard.shared.data.mapper.PipelineHistoryMapper
import com.orchestradashboard.shared.data.mapper.SystemStatusMapper
import com.orchestradashboard.shared.domain.model.ActivePipeline
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.SystemStatus
import com.orchestradashboard.shared.domain.repository.SystemEventData
import com.orchestradashboard.shared.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SystemRepositoryImpl(
    private val api: OrchestratorApi,
    private val statusMapper: SystemStatusMapper,
    private val pipelineMapper: ActivePipelineMapper,
    private val historyMapper: PipelineHistoryMapper,
) : SystemRepository {
    override suspend fun getSystemStatus(): Result<SystemStatus> = runCatching { statusMapper.toDomain(api.getStatus()) }

    override suspend fun getActivePipelines(): Result<List<ActivePipeline>> =
        runCatching { pipelineMapper.toDomainList(api.getPipelines()) }

    override suspend fun getPipelineHistory(): Result<List<PipelineResult>> =
        runCatching { historyMapper.toDomainList(api.getPipelineHistory()) }

    override fun observeSystemEvents(): Flow<SystemEventData> =
        api.connectEvents().map { event ->
            SystemEventData(
                ramPercent = event.ramPercent,
                cpuPercent = event.cpuPercent,
                thermal = event.thermal,
                step = event.step,
                status = event.status,
            )
        }
}
