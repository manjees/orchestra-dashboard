package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class AgentCommandResponse(
    val id: String,
    @JsonProperty("agent_id") val agentId: String,
    @JsonProperty("command_type") val commandType: String,
    val status: String,
    @JsonProperty("requested_at") val requestedAt: Long,
    @JsonProperty("requested_by") val requestedBy: String,
    @JsonProperty("executed_at") val executedAt: Long? = null,
    @JsonProperty("completed_at") val completedAt: Long? = null,
    @JsonProperty("failure_reason") val failureReason: String? = null,
)

data class CreateCommandRequest(
    @field:NotBlank(message = "agent_id must not be blank")
    @JsonProperty("agent_id") val agentId: String = "",
    @field:NotBlank(message = "command_type must not be blank")
    @JsonProperty("command_type") val commandType: String = "",
)
