package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class AgentEventMapper {
    fun toDomain(dto: AgentEventDto): AgentEvent {
        return AgentEvent(
            id = dto.id,
            agentId = dto.agentId,
            type = parseEventType(dto.type),
            payload = serializePayload(dto.payload),
            timestamp = dto.timestamp,
        )
    }

    fun toDomain(dtos: List<AgentEventDto>): List<AgentEvent> = dtos.map(::toDomain)

    private fun parseEventType(raw: String): EventType {
        return EventType.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: EventType.HEARTBEAT
    }

    private fun serializePayload(payload: Map<String, JsonElement>): String {
        return JsonObject(payload).toString()
    }
}
