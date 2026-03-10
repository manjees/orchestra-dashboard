package com.orchestradashboard.server.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class AgentEventMapper {
    private val objectMapper = ObjectMapper()

    fun toResponse(entity: AgentEventEntity): AgentEventResponse =
        AgentEventResponse(
            id = entity.id,
            agentId = entity.agentId,
            type = entity.type,
            payload = parsePayload(entity.payload),
            timestamp = entity.timestamp,
        )

    fun toResponseList(entities: List<AgentEventEntity>): List<AgentEventResponse> = entities.map { toResponse(it) }

    fun parsePayload(json: String): Map<String, Any> =
        if (json.isBlank()) {
            emptyMap()
        } else {
            try {
                objectMapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
            } catch (_: Exception) {
                emptyMap()
            }
        }

    fun serializePayload(map: Map<String, Any>): String = objectMapper.writeValueAsString(map)
}
