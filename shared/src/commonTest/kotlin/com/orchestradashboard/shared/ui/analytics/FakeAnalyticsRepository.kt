package com.orchestradashboard.shared.ui.analytics

import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import com.orchestradashboard.shared.domain.model.StepFailureRate
import com.orchestradashboard.shared.domain.repository.AnalyticsRepository
import kotlinx.coroutines.CompletableDeferred

class FakeAnalyticsRepository : AnalyticsRepository {
    var summaryResult: Result<PipelineAnalytics> =
        Result.success(PipelineAnalytics("", 0.0, 0.0, 0, 0))
    var stepFailuresResult: Result<List<StepFailureRate>> =
        Result.success(emptyList())
    var durationTrendsResult: Result<List<DurationTrend>> =
        Result.success(emptyList())

    var getSummaryCallCount = 0
        private set
    var getStepFailuresCallCount = 0
        private set
    var getDurationTrendsCallCount = 0
        private set
    var lastProject: String? = null
        private set
    var lastFrom: Long? = null
        private set
    var lastGranularity: String? = null
        private set

    private var summaryBlocker: CompletableDeferred<Unit>? = null

    fun blockSummary(): CompletableDeferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        summaryBlocker = deferred
        return deferred
    }

    override suspend fun getAnalyticsSummary(
        project: String,
        from: Long?,
        to: Long?,
    ): Result<PipelineAnalytics> {
        getSummaryCallCount++
        lastProject = project
        lastFrom = from
        summaryBlocker?.await()
        summaryBlocker = null
        return summaryResult
    }

    override suspend fun getStepFailureRates(project: String): Result<List<StepFailureRate>> {
        getStepFailuresCallCount++
        lastProject = project
        return stepFailuresResult
    }

    override suspend fun getDurationTrends(
        project: String,
        granularity: String,
    ): Result<List<DurationTrend>> {
        getDurationTrendsCallCount++
        lastProject = project
        lastGranularity = granularity
        return durationTrendsResult
    }
}
