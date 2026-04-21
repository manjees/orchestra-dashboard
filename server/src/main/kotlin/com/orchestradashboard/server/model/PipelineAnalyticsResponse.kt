package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PipelineAnalyticsResponse(
    val project: String,
    @JsonProperty("success_rate") val successRate: Double,
    @JsonProperty("avg_duration_sec") val avgDurationSec: Double,
    @JsonProperty("total_runs") val totalRuns: Int,
    @JsonProperty("failed_runs") val failedRuns: Int,
)

data class StepFailureRateResponse(
    @JsonProperty("step_name") val stepName: String,
    @JsonProperty("total_count") val totalCount: Int,
    @JsonProperty("failed_count") val failedCount: Int,
    @JsonProperty("failure_rate") val failureRate: Double,
)

data class DurationTrendResponse(
    val date: String,
    @JsonProperty("avg_duration_sec") val avgDurationSec: Double,
    @JsonProperty("run_count") val runCount: Int,
)
