package com.orchestradashboard.server.model

import org.springframework.stereotype.Component

@Component
class AgentCommandMapper {
    fun toResponse(entity: AgentCommandEntity): AgentCommandResponse =
        AgentCommandResponse(
            id = entity.id,
            agentId = entity.agentId,
            commandType = entity.commandType,
            status = entity.status,
            requestedAt = entity.requestedAt,
            requestedBy = entity.requestedBy,
            executedAt = entity.executedAt,
            completedAt = entity.completedAt,
            failureReason = entity.failureReason,
        )

    fun toResponseList(entities: List<AgentCommandEntity>): List<AgentCommandResponse> = entities.map { toResponse(it) }
}
