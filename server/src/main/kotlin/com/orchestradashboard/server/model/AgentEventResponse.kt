package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class AgentEventResponse(
    val id: String,
    @JsonProperty("agent_id") val agentId: String,
    val type: String,
    val payload: Map<String, Any> = emptyMap(),
    val timestamp: Long,
)

data class CreateEventRequest(
    @field:NotBlank(message = "agent_id must not be blank")
    @JsonProperty("agent_id") val agentId: String,
    @field:NotBlank(message = "type must not be blank")
    val type: String,
    val payload: Map<String, Any> = emptyMap(),
    val timestamp: Long? = null,
)
