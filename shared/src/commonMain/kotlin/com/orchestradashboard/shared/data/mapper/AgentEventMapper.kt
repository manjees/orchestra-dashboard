package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class AgentEventMapper {
    fun toDomain(dto: AgentEventDto): AgentEvent {
        return AgentEvent(
            id = dto.id,
            agentId = dto.agentId,
            type = parseEventType(dto.type),
            payload = dto.payload.toString(),
            timestamp = dto.timestamp,
        )
    }

    fun toDomain(dtos: List<AgentEventDto>): List<AgentEvent> = dtos.map(::toDomain)

    fun toDto(domain: AgentEvent): AgentEventDto {
        return AgentEventDto(
            id = domain.id,
            agentId = domain.agentId,
            type = domain.type.name,
            payload = parsePayload(domain.payload),
            timestamp = domain.timestamp,
        )
    }

    fun toDto(domains: List<AgentEvent>): List<AgentEventDto> = domains.map(::toDto)

    private fun parseEventType(raw: String): EventType {
        return EventType.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: EventType.HEARTBEAT
    }

    private fun parsePayload(raw: String): JsonObject {
        if (raw.isBlank()) return JsonObject(emptyMap())
        return try {
            Json.parseToJsonElement(raw).jsonObject
        } catch (_: Exception) {
            JsonObject(emptyMap())
        }
    }
}
