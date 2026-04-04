package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParallelPipelineGroupDto(
    @SerialName("parent_pipeline_id") val parentPipelineId: String,
    val lanes: List<OrchestratorPipelineDto>,
    val dependencies: List<PipelineDependencyDto> = emptyList(),
)

@Serializable
data class PipelineDependencyDto(
    @SerialName("source_lane_id") val sourceLaneId: String,
    @SerialName("target_lane_id") val targetLaneId: String,
    val type: String,
)
