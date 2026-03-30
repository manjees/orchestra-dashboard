package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Metric
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.domain.repository.MetricRepository

class GetAggregatedMetricsUseCase(
    private val metricRepository: MetricRepository,
) {
    suspend operator fun invoke(
        agentId: String,
        timeRange: TimeRange,
    ): Result<List<Metric>> = metricRepository.getAggregatedMetrics(agentId, timeRange)
}
