package com.orchestradashboard.shared.domain.model

data class PipelineAnalytics(
    val project: String,
    val successRate: Double,
    val avgDurationSec: Double,
    val totalRuns: Int,
    val failedRuns: Int,
)
