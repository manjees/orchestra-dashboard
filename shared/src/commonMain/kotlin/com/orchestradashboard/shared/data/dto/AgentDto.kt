package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object for agent data received from the API.
 * Maps directly to the JSON shape returned by the server.
 */
@Serializable
data class AgentDto(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    @SerialName("last_heartbeat") val lastHeartbeat: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Wrapper for paginated agent list responses.
 */
@Serializable
data class AgentListDto(
    val agents: List<AgentDto>,
    val total: Int
)
