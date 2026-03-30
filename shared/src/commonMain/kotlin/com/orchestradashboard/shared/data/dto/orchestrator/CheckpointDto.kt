package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckpointDto(
    val id: String,
    @SerialName("pipeline_id") val pipelineId: String,
    @SerialName("created_at") val createdAt: String,
    val step: String,
    val status: String,
)
