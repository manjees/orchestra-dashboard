package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentEventMapperTest {
    private val mapper = AgentEventMapper()

    @Test
    fun `toDomain maps complete AgentEventDto correctly`() {
        val dto =
            AgentEventDto(
                id = "evt-1",
                agentId = "agent-1",
                type = "STATUS_CHANGE",
                payload = JsonObject(mapOf("key" to JsonPrimitive("value"))),
                timestamp = 1000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals("evt-1", result.id)
        assertEquals("agent-1", result.agentId)
        assertEquals(EventType.STATUS_CHANGE, result.type)
        assertEquals("""{"key":"value"}""", result.payload)
        assertEquals(1000L, result.timestamp)
    }

    @Test
    fun `toDomain maps empty payload to empty JSON object string`() {
        val dto =
            AgentEventDto(
                id = "evt-2",
                agentId = "agent-1",
                type = "HEARTBEAT",
                payload = JsonObject(emptyMap()),
                timestamp = 2000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals("{}", result.payload)
    }

    @Test
    fun `toDomain handles unknown EventType by defaulting to HEARTBEAT`() {
        val dto =
            AgentEventDto(
                id = "evt-3",
                agentId = "agent-1",
                type = "BOGUS",
                payload = JsonObject(emptyMap()),
                timestamp = 3000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(EventType.HEARTBEAT, result.type)
    }

    @Test
    fun `toDomain handles case-insensitive type parsing`() {
        val dto =
            AgentEventDto(
                id = "evt-4",
                agentId = "agent-1",
                type = "error",
                payload = JsonObject(emptyMap()),
                timestamp = 4000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(EventType.ERROR, result.type)
    }

    @Test
    fun `toDomain list variant maps all elements`() {
        val dtos =
            listOf(
                AgentEventDto("e1", "a1", "HEARTBEAT", JsonObject(emptyMap()), 100L),
                AgentEventDto("e2", "a2", "ERROR", JsonObject(emptyMap()), 200L),
                AgentEventDto("e3", "a3", "STATUS_CHANGE", JsonObject(emptyMap()), 300L),
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

    @Test
    fun `toDto maps domain AgentEvent to DTO correctly`() {
        val domain =
            AgentEvent(
                id = "evt-1",
                agentId = "agent-1",
                type = EventType.STATUS_CHANGE,
                payload = """{"k":"v"}""",
                timestamp = 1000L,
            )

        val result = mapper.toDto(domain)

        assertEquals("evt-1", result.id)
        assertEquals("agent-1", result.agentId)
        assertEquals("STATUS_CHANGE", result.type)
        assertEquals(JsonObject(mapOf("k" to JsonPrimitive("v"))), result.payload)
        assertEquals(1000L, result.timestamp)
    }

    @Test
    fun `toDto maps invalid JSON payload to empty JsonObject`() {
        val domain =
            AgentEvent(
                id = "evt-2",
                agentId = "agent-1",
                type = EventType.ERROR,
                payload = "not json",
                timestamp = 2000L,
            )

        val result = mapper.toDto(domain)

        assertEquals(JsonObject(emptyMap()), result.payload)
    }

    @Test
    fun `toDto maps empty string payload to empty JsonObject`() {
        val domain =
            AgentEvent(
                id = "evt-3",
                agentId = "agent-1",
                type = EventType.HEARTBEAT,
                payload = "",
                timestamp = 3000L,
            )

        val result = mapper.toDto(domain)

        assertEquals(JsonObject(emptyMap()), result.payload)
    }

    @Test
    fun `toDto round-trips correctly`() {
        val originalDto =
            AgentEventDto(
                id = "rt",
                agentId = "a1",
                type = "PIPELINE_STARTED",
                payload = JsonObject(mapOf("pipeline" to JsonPrimitive("build"))),
                timestamp = 999L,
            )

        val roundTripped = mapper.toDto(mapper.toDomain(originalDto))

        assertEquals(originalDto, roundTripped)
    }
}
