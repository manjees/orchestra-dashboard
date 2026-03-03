package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * REST response DTO for agent data.
 * Decoupled from [AgentEntity] to allow independent API versioning.
 */
data class AgentResponse(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    @JsonProperty("last_heartbeat") val lastHeartbeat: Long,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * REST request DTO for creating a new agent.
 */
data class CreateAgentRequest(
    val id: String,
    val name: String,
    val type: String,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * REST request DTO for updating an agent's status.
 */
data class UpdateAgentStatusRequest(
    val status: String,
    @JsonProperty("last_heartbeat") val lastHeartbeat: Long = System.currentTimeMillis()
)

/** Extension to convert [AgentEntity] to [AgentResponse] */
fun AgentEntity.toResponse(): AgentResponse = AgentResponse(
    id = this.agentId,
    name = this.name,
    type = this.type,
    status = this.status,
    lastHeartbeat = this.lastHeartbeat,
    metadata = this.metadata
)
