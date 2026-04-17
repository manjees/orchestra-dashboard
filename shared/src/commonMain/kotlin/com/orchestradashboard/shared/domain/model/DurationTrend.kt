package com.orchestradashboard.shared.domain.model

data class DurationTrend(
    val date: String,
    val avgDurationSec: Double,
    val runCount: Int,
)
