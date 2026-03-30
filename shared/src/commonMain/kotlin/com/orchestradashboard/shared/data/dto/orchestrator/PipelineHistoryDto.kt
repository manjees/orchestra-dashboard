package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PipelineHistoryDto(
    val id: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("issue_num") val issueNum: Int,
    val status: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("elapsed_total_sec") val elapsedTotalSec: Double,
)
