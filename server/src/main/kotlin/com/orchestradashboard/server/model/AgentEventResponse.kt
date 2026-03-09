package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AgentEventResponse(
    val id: String,
    @JsonProperty("agent_id") val agentId: String,
    val type: String,
    val payload: String,
    val timestamp: Long,
)

data class CreateEventRequest(
    @JsonProperty("agent_id") val agentId: String,
    val type: String,
    val payload: String = "",
)
