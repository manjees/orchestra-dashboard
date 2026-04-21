package com.orchestradashboard.shared.data.dto.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsSummaryDto(
    val project: String,
    @SerialName("success_rate") val successRate: Double,
    @SerialName("avg_duration_sec") val avgDurationSec: Double,
    @SerialName("total_runs") val totalRuns: Int,
    @SerialName("failed_runs") val failedRuns: Int,
)
