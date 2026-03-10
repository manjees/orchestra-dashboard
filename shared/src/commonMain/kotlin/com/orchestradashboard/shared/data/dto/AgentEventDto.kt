package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AgentEventDto(
    val id: String,
    @SerialName("agent_id") val agentId: String,
    val type: String,
    val payload: JsonObject = JsonObject(emptyMap()),
    val timestamp: Long,
)
