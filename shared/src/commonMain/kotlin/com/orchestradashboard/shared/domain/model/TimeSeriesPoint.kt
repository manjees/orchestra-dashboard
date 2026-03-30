package com.orchestradashboard.shared.domain.model

data class TimeSeriesPoint(
    val timestamp: Long,
    val value: Double,
)
