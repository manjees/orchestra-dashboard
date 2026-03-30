package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TimeSeriesDataResponse(
    @JsonProperty("agentId") val agentId: String,
    @JsonProperty("metricName") val metricName: String,
    @JsonProperty("dataPoints") val dataPoints: List<DataPointResponse>,
    @JsonProperty("average") val average: Double?,
    @JsonProperty("min") val min: Double?,
    @JsonProperty("max") val max: Double?,
    @JsonProperty("sampleCount") val sampleCount: Int,
    @JsonProperty("fromTimestamp") val fromTimestamp: Long,
    @JsonProperty("toTimestamp") val toTimestamp: Long,
)

data class DataPointResponse(
    @JsonProperty("timestamp") val timestamp: Long,
    @JsonProperty("value") val value: Double,
)
