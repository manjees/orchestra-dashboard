package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PipelineHistoryResponse(
    val id: String,
    @JsonProperty("project_name") val projectName: String,
    @JsonProperty("issue_num") val issueNum: Int,
    @JsonProperty("issue_title") val issueTitle: String,
    val mode: String,
    val status: String,
    @JsonProperty("started_at") val startedAt: Long,
    @JsonProperty("completed_at") val completedAt: Long?,
    @JsonProperty("elapsed_total_sec") val elapsedTotalSec: Double,
    @JsonProperty("pr_url") val prUrl: String?,
    val steps: List<StepHistoryResponse>,
)

data class StepHistoryResponse(
    @JsonProperty("step_name") val stepName: String,
    val status: String,
    @JsonProperty("elapsed_sec") val elapsedSec: Double,
    @JsonProperty("fail_detail") val failDetail: String?,
)
