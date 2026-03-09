package com.orchestradashboard.server.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class AgentMapper {
    private val objectMapper = ObjectMapper()

    fun toResponse(entity: AgentEntity): AgentResponse =
        AgentResponse(
            id = entity.id,
            name = entity.name,
            type = entity.type,
            status = entity.status,
            lastHeartbeat = entity.lastHeartbeat,
            metadata = parseMetadata(entity.metadata),
        )

    fun toResponseList(entities: List<AgentEntity>): List<AgentResponse> = entities.map { toResponse(it) }

    fun parseMetadata(json: String): Map<String, String> =
        if (json.isBlank()) {
            emptyMap()
        } else {
            try {
                objectMapper.readValue(json, object : TypeReference<Map<String, String>>() {})
            } catch (_: Exception) {
                emptyMap()
            }
        }

    fun serializeMetadata(map: Map<String, String>): String = objectMapper.writeValueAsString(map)
}
