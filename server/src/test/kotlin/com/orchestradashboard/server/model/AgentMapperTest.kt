package com.orchestradashboard.server.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AgentMapperTest {
    private val mapper = AgentMapper()

    @Test
    fun `toResponse maps all entity fields correctly`() {
        val entity =
            AgentEntity(
                id = "abc-123",
                name = "Worker Alpha",
                type = "WORKER",
                status = "RUNNING",
                lastHeartbeat = 1700000000L,
                metadata = """{"env":"prod","version":"1.0"}""",
            )

        val response = mapper.toResponse(entity)

        assertEquals("abc-123", response.id)
        assertEquals("Worker Alpha", response.name)
        assertEquals("WORKER", response.type)
        assertEquals("RUNNING", response.status)
        assertEquals(1700000000L, response.lastHeartbeat)
        assertEquals(mapOf("env" to "prod", "version" to "1.0"), response.metadata)
    }

    @Test
    fun `toResponse handles empty metadata`() {
        val entity =
            AgentEntity(
                id = "abc-123",
                name = "Worker Alpha",
                type = "WORKER",
                status = "OFFLINE",
                lastHeartbeat = 0L,
                metadata = "{}",
            )

        val response = mapper.toResponse(entity)

        assertTrue(response.metadata.isEmpty())
    }

    @Test
    fun `toResponse handles null metadata column`() {
        val entity =
            AgentEntity(
                id = "abc-123",
                name = "Worker Alpha",
                type = "WORKER",
                status = "OFFLINE",
                lastHeartbeat = 0L,
                metadata = "",
            )

        val response = mapper.toResponse(entity)

        assertTrue(response.metadata.isEmpty())
    }

    @Test
    fun `toResponseList maps list of entities`() {
        val entities =
            listOf(
                AgentEntity(id = "a1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L),
                AgentEntity(id = "a2", name = "Beta", type = "PLANNER", status = "IDLE", lastHeartbeat = 200L),
            )

        val responses = mapper.toResponseList(entities)

        assertEquals(2, responses.size)
        assertEquals("a1", responses[0].id)
        assertEquals("a2", responses[1].id)
    }
}
