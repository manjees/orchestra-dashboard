package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing a single pipeline history entry from the BFF server.
 * Matches the Spring Boot `PipelineHistoryResponse` Jackson JSON (snake_case via `@JsonProperty`).
 */
@Serializable
data class PipelineHistoryDetailDto(
    val id: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("issue_num") val issueNum: Int,
    @SerialName("issue_title") val issueTitle: String,
    val mode: String,
    val status: String,
    @SerialName("started_at") val startedAt: Long,
    @SerialName("completed_at") val completedAt: Long? = null,
    @SerialName("elapsed_total_sec") val elapsedTotalSec: Double,
    @SerialName("pr_url") val prUrl: String? = null,
    val steps: List<StepHistoryDto> = emptyList(),
)

@Serializable
data class StepHistoryDto(
    @SerialName("step_name") val stepName: String,
    val status: String,
    @SerialName("elapsed_sec") val elapsedSec: Double,
    @SerialName("fail_detail") val failDetail: String? = null,
)
