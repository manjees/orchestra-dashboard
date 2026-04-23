package com.orchestradashboard.shared.ui.analytics

import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.model.PeriodFilter
import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import com.orchestradashboard.shared.domain.model.StepFailureRate

data class AnalyticsUiState(
    val summary: PipelineAnalytics? = null,
    val durationTrends: List<DurationTrend> = emptyList(),
    val stepFailures: List<StepFailureRate> = emptyList(),
    val selectedPeriod: PeriodFilter = PeriodFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val hasData: Boolean get() = summary != null
}
