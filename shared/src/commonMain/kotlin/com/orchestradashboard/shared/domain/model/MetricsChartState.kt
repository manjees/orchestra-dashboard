package com.orchestradashboard.shared.domain.model

data class MetricsChartState(
    val points: List<TimeSeriesPoint> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.Last24Hours,
    val availableMetricNames: List<String> = emptyList(),
    val selectedMetricName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
