package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentEventMapperTest {
    private val mapper = AgentEventMapper()

    @Test
    fun `toDomain maps valid DTO with all fields correctly`() {
        val dto =
            AgentEventDto(
                id = "evt-1",
                agentId = "agent-1",
                type = "STATUS_CHANGE",
                payload = mapOf("status" to JsonPrimitive("RUNNING")),
                timestamp = 1700000000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals("evt-1", result.id)
        assertEquals("agent-1", result.agentId)
        assertEquals(EventType.STATUS_CHANGE, result.type)
        assertEquals(1700000000L, result.timestamp)
        assertTrue(result.payload.contains("status"))
    }

    @Test
    fun `toDomain handles unknown event type by defaulting to HEARTBEAT`() {
        val dto =
            AgentEventDto(
                id = "evt-2",
                agentId = "agent-1",
                type = "UNKNOWN_TYPE",
                timestamp = 0L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(EventType.HEARTBEAT, result.type)
    }

    @Test
    fun `toDomain handles case-insensitive type parsing`() {
        val dto =
            AgentEventDto(
                id = "evt-3",
                agentId = "agent-1",
                type = "pipeline_started",
                timestamp = 0L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(EventType.PIPELINE_STARTED, result.type)
    }

    @Test
    fun `toDomain serializes payload map to JSON string`() {
        val dto =
            AgentEventDto(
                id = "evt-4",
                agentId = "agent-1",
                type = "ERROR",
                payload =
                    mapOf(
                        "message" to JsonPrimitive("Something failed"),
                        "code" to JsonPrimitive(500),
                    ),
                timestamp = 0L,
            )

        val result = mapper.toDomain(dto)

        assertTrue(result.payload.contains("Something failed"))
        assertTrue(result.payload.contains("500"))
    }

    @Test
    fun `toDomain handles empty payload map`() {
        val dto =
            AgentEventDto(
                id = "evt-5",
                agentId = "agent-1",
                type = "HEARTBEAT",
                payload = emptyMap(),
                timestamp = 0L,
            )

        val result = mapper.toDomain(dto)

        assertEquals("{}", result.payload)
    }

    @Test
    fun `toDomain list variant maps all elements`() {
        val dtos =
            listOf(
                AgentEventDto("e1", "a1", "HEARTBEAT", emptyMap(), 100L),
                AgentEventDto("e2", "a1", "ERROR", emptyMap(), 200L),
                AgentEventDto("e3", "a2", "STATUS_CHANGE", emptyMap(), 300L),
            )

        val results = mapper.toDomain(dtos)

        assertEquals(3, results.size)
        assertEquals("e1", results[0].id)
        assertEquals("e2", results[1].id)
        assertEquals("e3", results[2].id)
    }

    @Test
    fun `toDomain list variant maps empty list`() {
        val results = mapper.toDomain(emptyList())
        assertTrue(results.isEmpty())
    }
}
