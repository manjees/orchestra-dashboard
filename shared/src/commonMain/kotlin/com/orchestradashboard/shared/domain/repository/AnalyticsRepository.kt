package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import com.orchestradashboard.shared.domain.model.StepFailureRate

interface AnalyticsRepository {
    suspend fun getAnalyticsSummary(
        project: String,
        from: Long? = null,
        to: Long? = null,
    ): Result<PipelineAnalytics>

    suspend fun getStepFailureRates(project: String): Result<List<StepFailureRate>>

    suspend fun getDurationTrends(
        project: String,
        granularity: String = "day",
    ): Result<List<DurationTrend>>
}
