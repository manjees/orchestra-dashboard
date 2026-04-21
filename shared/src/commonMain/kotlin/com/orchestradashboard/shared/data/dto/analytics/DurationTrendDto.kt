package com.orchestradashboard.shared.data.dto.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DurationTrendDto(
    val date: String,
    @SerialName("avg_duration_sec") val avgDurationSec: Double,
    @SerialName("run_count") val runCount: Int,
)
