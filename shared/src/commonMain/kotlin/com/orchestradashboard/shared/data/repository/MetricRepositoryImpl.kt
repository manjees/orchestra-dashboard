package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.Metric
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.domain.repository.MetricRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class MetricRepositoryImpl(
    private val apiClient: DashboardApi,
) : MetricRepository {
    override fun observeMetrics(agentId: String): Flow<List<Metric>> =
        flow {
            throw UnsupportedOperationException("Not yet implemented")
        }

    override suspend fun getFleetMetrics(): Result<List<Metric>> =
        runCatching {
            emptyList()
        }

    override suspend fun getAggregatedMetrics(
        agentId: String,
        timeRange: TimeRange,
    ): Result<List<Metric>> =
        runCatching {
            val now = Clock.System.now().toEpochMilliseconds()
            val startTime = now - (timeRange.hours * 3_600_000L)
            val dtos = apiClient.getAggregatedMetrics(agentId, startTime, now)
            dtos.map { dto ->
                Metric(
                    agentId = dto.agentId,
                    name = dto.name,
                    value = dto.value,
                    unit = dto.unit,
                    timestamp = dto.timestamp,
                )
            }
        }
}
