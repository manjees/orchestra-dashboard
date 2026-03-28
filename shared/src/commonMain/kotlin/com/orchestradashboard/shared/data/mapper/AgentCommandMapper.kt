package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentCommandDto
import com.orchestradashboard.shared.domain.model.AgentCommand
import com.orchestradashboard.shared.domain.model.CommandStatus
import com.orchestradashboard.shared.domain.model.CommandType

class AgentCommandMapper {
    fun toDomain(dto: AgentCommandDto): AgentCommand =
        AgentCommand(
            id = dto.id,
            agentId = dto.agentId,
            commandType = CommandType.valueOf(dto.commandType),
            status = CommandStatus.valueOf(dto.status),
            requestedAt = dto.requestedAt,
            requestedBy = dto.requestedBy,
            executedAt = dto.executedAt,
            completedAt = dto.completedAt,
            failureReason = dto.failureReason,
        )

    fun toDto(domain: AgentCommand): AgentCommandDto =
        AgentCommandDto(
            id = domain.id,
            agentId = domain.agentId,
            commandType = domain.commandType.name,
            status = domain.status.name,
            requestedAt = domain.requestedAt,
            requestedBy = domain.requestedBy,
            executedAt = domain.executedAt,
            completedAt = domain.completedAt,
            failureReason = domain.failureReason,
        )
}
