package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.domain.model.Agent

class AgentMapper {
    fun toDomain(dto: AgentDto): Agent {
        return Agent(
            id = dto.id,
            name = dto.name,
            type = parseAgentType(dto.type),
            status = parseAgentStatus(dto.status),
            lastHeartbeat = dto.lastHeartbeat,
            createdAt = dto.createdAt,
            metadata = dto.metadata,
        )
    }

    fun toDomain(dtos: List<AgentDto>): List<Agent> = dtos.map(::toDomain)

    fun toDto(domain: Agent): AgentDto {
        return AgentDto(
            id = domain.id,
            name = domain.name,
            type = domain.type.name,
            status = domain.status.name,
            lastHeartbeat = domain.lastHeartbeat,
            createdAt = domain.createdAt,
            metadata = domain.metadata,
        )
    }

    fun toDto(domains: List<Agent>): List<AgentDto> = domains.map(::toDto)

    private fun parseAgentType(raw: String): Agent.AgentType {
        return Agent.AgentType.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: Agent.AgentType.WORKER
    }

    private fun parseAgentStatus(raw: String): Agent.AgentStatus {
        return Agent.AgentStatus.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: Agent.AgentStatus.OFFLINE
    }
}
