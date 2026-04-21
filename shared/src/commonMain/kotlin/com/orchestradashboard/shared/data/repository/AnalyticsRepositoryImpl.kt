package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.AnalyticsMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import com.orchestradashboard.shared.domain.model.StepFailureRate
import com.orchestradashboard.shared.domain.repository.AnalyticsRepository

class AnalyticsRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: AnalyticsMapper,
) : AnalyticsRepository {
    override suspend fun getAnalyticsSummary(
        project: String,
        from: Long?,
        to: Long?,
    ): Result<PipelineAnalytics> =
        runCatching {
            mapper.toDomain(api.getAnalyticsSummary(project, from, to))
        }

    override suspend fun getStepFailureRates(project: String): Result<List<StepFailureRate>> =
        runCatching {
            mapper.toStepFailureRates(api.getStepFailures(project))
        }

    override suspend fun getDurationTrends(
        project: String,
        granularity: String,
    ): Result<List<DurationTrend>> =
        runCatching {
            mapper.toDurationTrends(api.getDurationTrends(project, granularity))
        }
}
