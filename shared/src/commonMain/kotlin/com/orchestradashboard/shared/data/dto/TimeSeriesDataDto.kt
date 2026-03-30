package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimeSeriesDataDto(
    @SerialName("agent_id") val agentId: String,
    @SerialName("metric_name") val metricName: String,
    @SerialName("avg_value") val avgValue: Double,
    @SerialName("min_value") val minValue: Double,
    @SerialName("max_value") val maxValue: Double,
    val count: Int,
    @SerialName("timestamp_bucket") val timestampBucket: Long,
    @SerialName("created_at") val createdAt: Long,
)
