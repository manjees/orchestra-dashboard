package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AggregatedMetricDto(
    @SerialName("agent_id") val agentId: String,
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: Long,
)
