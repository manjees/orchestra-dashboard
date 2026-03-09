package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AgentResponse(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    @JsonProperty("last_heartbeat") val lastHeartbeat: Long,
    val metadata: Map<String, String> = emptyMap(),
)

data class AgentRegistrationRequest(
    val id: String? = null,
    val name: String,
    val type: String,
    val metadata: Map<String, String> = emptyMap(),
)

data class HeartbeatRequest(
    val status: String,
)
