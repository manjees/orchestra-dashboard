package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object for metric data received from the API.
 */
@Serializable
data class MetricDto(
    @SerialName("agent_id") val agentId: String,
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: Long
)

/**
 * WebSocket event envelope for real-time agent updates.
 */
@Serializable
data class AgentEventDto(
    val type: String,
    @SerialName("agent_id") val agentId: String,
    val status: String? = null,
    val timestamp: Long,
    val payload: Map<String, String> = emptyMap()
)
