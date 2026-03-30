package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AggregatedMetricResponse(
    val id: String,
    @JsonProperty("agent_id") val agentId: String,
    @JsonProperty("metric_name") val metricName: String,
    @JsonProperty("avg_value") val avgValue: Double,
    @JsonProperty("min_value") val minValue: Double,
    @JsonProperty("max_value") val maxValue: Double,
    val count: Int,
    @JsonProperty("timestamp_bucket") val timestampBucket: Long,
    @JsonProperty("created_at") val createdAt: Long,
)
