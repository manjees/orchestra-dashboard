package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrchestratorPipelineDto(
    val id: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("issue_num") val issueNum: Int,
    @SerialName("issue_title") val issueTitle: String,
    val mode: String,
    val status: String,
    @SerialName("current_step") val currentStep: String? = null,
    @SerialName("started_at") val startedAt: String,
    val steps: List<PipelineStepDto>,
    @SerialName("elapsed_total_sec") val elapsedTotalSec: Double,
)

@Serializable
data class PipelineStepDto(
    val name: String,
    val status: String,
    @SerialName("elapsed_sec") val elapsedSec: Double,
)
