package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.domain.model.Agent

/**
 * Maps between [AgentDto] (network layer) and [Agent] (domain layer).
 * Stateless — safe to share across threads and coroutines.
 */
class AgentMapper {

    /**
     * Converts an API DTO to a domain model.
     * Unknown enum values are mapped to safe defaults to avoid crashes.
     *
     * @param dto The raw DTO from the API
     * @return Corresponding domain model
     */
    fun toDomain(dto: AgentDto): Agent {
        return Agent(
            id = dto.id,
            name = dto.name,
            type = parseAgentType(dto.type),
            status = parseAgentStatus(dto.status),
            lastHeartbeat = dto.lastHeartbeat,
            metadata = dto.metadata
        )
    }

    /**
     * Converts a list of DTOs to domain models.
     *
     * @param dtos List of raw DTOs
     * @return List of domain models
     */
    fun toDomain(dtos: List<AgentDto>): List<Agent> = dtos.map(::toDomain)

    private fun parseAgentType(raw: String): Agent.AgentType {
        return Agent.AgentType.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: Agent.AgentType.WORKER
    }

    private fun parseAgentStatus(raw: String): Agent.AgentStatus {
        return Agent.AgentStatus.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: Agent.AgentStatus.OFFLINE
    }
}
