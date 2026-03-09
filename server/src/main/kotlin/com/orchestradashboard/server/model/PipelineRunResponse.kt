package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PipelineStepResponse(
    val name: String,
    val status: String,
    val detail: String,
    @JsonProperty("elapsed_ms") val elapsedMs: Long,
)

data class PipelineRunResponse(
    val id: String,
    @JsonProperty("agent_id") val agentId: String,
    @JsonProperty("pipeline_name") val pipelineName: String,
    val status: String,
    val steps: List<PipelineStepResponse>,
    @JsonProperty("started_at") val startedAt: Long,
    @JsonProperty("finished_at") val finishedAt: Long?,
    @JsonProperty("trigger_info") val triggerInfo: String,
)

data class CreatePipelineRunRequest(
    val id: String? = null,
    @JsonProperty("agent_id") val agentId: String,
    @JsonProperty("pipeline_name") val pipelineName: String,
    @JsonProperty("trigger_info") val triggerInfo: String = "",
    val steps: List<PipelineStepRequest> = emptyList(),
)

data class PipelineStepRequest(
    val name: String,
    val status: String = "PENDING",
    val detail: String = "",
    @JsonProperty("elapsed_ms") val elapsedMs: Long = 0L,
)

data class UpdateStatusRequest(
    val status: String,
)
