package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.PipelineRunMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PipelineRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: PipelineRunMapper,
    private val pollingIntervalMs: Long = 5_000L,
) : PipelineRepository {
    override fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>> =
        flow {
            while (true) {
                val runs = api.getPipelineRuns(agentId)
                emit(mapper.toDomain(runs))
                delay(pollingIntervalMs)
            }
        }

    override suspend fun getPipelineRun(runId: String): Result<PipelineRun> =
        runCatching {
            mapper.toDomain(api.getPipelineRun(runId))
        }

    override fun observeActivePipelines(): Flow<List<PipelineRun>> =
        flow {
            while (true) {
                val runs = api.getPipelineRuns(agentId = null)
                val active =
                    runs.filter {
                        it.status.equals("RUNNING", ignoreCase = true) ||
                            it.status.equals("QUEUED", ignoreCase = true)
                    }
                emit(mapper.toDomain(active))
                delay(pollingIntervalMs)
            }
        }
}
