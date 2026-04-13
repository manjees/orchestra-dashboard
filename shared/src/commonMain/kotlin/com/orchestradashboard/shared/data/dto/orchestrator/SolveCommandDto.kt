package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SolveCommandRequestDto(
    @SerialName("project_name") val projectName: String,
    @SerialName("issue_numbers") val issueNumbers: List<Int>,
    val mode: String,
    val parallel: Boolean,
)

@Serializable
data class SolveCommandResponseDto(
    @SerialName("pipeline_id") val pipelineId: String,
    val status: String,
)
