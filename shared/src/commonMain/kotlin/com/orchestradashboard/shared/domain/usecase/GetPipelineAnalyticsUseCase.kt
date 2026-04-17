package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import com.orchestradashboard.shared.domain.repository.AnalyticsRepository

/**
 * Retrieves pipeline analytics summary for a given project.
 */
class GetPipelineAnalyticsUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    suspend operator fun invoke(
        project: String,
        from: Long? = null,
        to: Long? = null,
    ): Result<PipelineAnalytics> = analyticsRepository.getAnalyticsSummary(project, from, to)
}
