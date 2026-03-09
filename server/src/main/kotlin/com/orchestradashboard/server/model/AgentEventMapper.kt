package com.orchestradashboard.server.model

import org.springframework.stereotype.Component

@Component
class AgentEventMapper {
    fun toResponse(entity: AgentEventEntity): AgentEventResponse =
        AgentEventResponse(
            id = entity.id,
            agentId = entity.agentId,
            type = entity.type,
            payload = entity.payload,
            timestamp = entity.timestamp,
        )

    fun toResponseList(entities: List<AgentEventEntity>): List<AgentEventResponse> = entities.map { toResponse(it) }
}
