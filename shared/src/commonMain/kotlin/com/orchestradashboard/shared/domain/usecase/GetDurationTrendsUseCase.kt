package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.repository.AnalyticsRepository

/**
 * Retrieves duration trend data for a given project over time.
 */
class GetDurationTrendsUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    suspend operator fun invoke(
        project: String,
        granularity: String = "day",
    ): Result<List<DurationTrend>> = analyticsRepository.getDurationTrends(project, granularity)
}
