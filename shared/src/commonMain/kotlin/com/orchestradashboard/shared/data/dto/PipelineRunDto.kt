package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PipelineStepDto(
    val name: String,
    val status: String,
    val detail: String,
    @SerialName("elapsed_ms") val elapsedMs: Long,
)

@Serializable
data class PipelineRunDto(
    val id: String,
    @SerialName("agent_id") val agentId: String,
    @SerialName("pipeline_name") val pipelineName: String,
    val status: String,
    val steps: List<PipelineStepDto> = emptyList(),
    @SerialName("started_at") val startedAt: Long,
    @SerialName("finished_at") val finishedAt: Long? = null,
    @SerialName("trigger_info") val triggerInfo: String = "",
)

@Serializable
data class PipelineRunPageDto(
    val content: List<PipelineRunDto>,
)
