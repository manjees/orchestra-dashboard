package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.PipelineRunMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PipelineRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: PipelineRunMapper,
) : PipelineRepository {
    override fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>> =
        api.observePipelineRuns(agentId)
            .map { dtos -> mapper.toDomain(dtos) }

    override suspend fun getPipelineRun(runId: String): Result<PipelineRun> =
        runCatching {
            mapper.toDomain(api.getPipelineRun(runId))
        }

    override fun observeActivePipelines(): Flow<List<PipelineRun>> =
        api.observePipelineRuns()
            .map { dtos ->
                mapper.toDomain(dtos).filter {
                    it.status == PipelineRunStatus.RUNNING || it.status == PipelineRunStatus.QUEUED
                }
            }
}
