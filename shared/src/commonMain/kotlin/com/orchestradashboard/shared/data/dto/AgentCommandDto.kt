package com.orchestradashboard.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgentCommandDto(
    val id: String,
    @SerialName("agent_id") val agentId: String,
    @SerialName("command_type") val commandType: String,
    val status: String,
    @SerialName("requested_at") val requestedAt: Long,
    @SerialName("requested_by") val requestedBy: String,
    @SerialName("executed_at") val executedAt: Long? = null,
    @SerialName("completed_at") val completedAt: Long? = null,
    @SerialName("failure_reason") val failureReason: String? = null,
)

@Serializable
data class CreateCommandDto(
    @SerialName("agent_id") val agentId: String,
    @SerialName("command_type") val commandType: String,
)
