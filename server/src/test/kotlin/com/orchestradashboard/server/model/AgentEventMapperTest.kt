package com.orchestradashboard.server.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AgentEventMapperTest {
    private val mapper = AgentEventMapper()

    @Test
    fun `toResponse maps all entity fields correctly`() {
        val entity =
            AgentEventEntity(
                id = "evt-1",
                agentId = "agent-1",
                type = "STATUS_CHANGE",
                payload = """{"from":"IDLE","to":"RUNNING"}""",
                timestamp = 1700000000L,
            )

        val response = mapper.toResponse(entity)

        assertEquals("evt-1", response.id)
        assertEquals("agent-1", response.agentId)
        assertEquals("STATUS_CHANGE", response.type)
        assertEquals(mapOf("from" to "IDLE", "to" to "RUNNING"), response.payload)
        assertEquals(1700000000L, response.timestamp)
    }

    @Test
    fun `toResponseList maps list of entities`() {
        val entities =
            listOf(
                AgentEventEntity(id = "e1", agentId = "a1", type = "HEARTBEAT", payload = "", timestamp = 100L),
                AgentEventEntity(id = "e2", agentId = "a2", type = "ERROR", payload = "fail", timestamp = 200L),
            )

        val responses = mapper.toResponseList(entities)

        assertEquals(2, responses.size)
        assertEquals("e1", responses[0].id)
        assertEquals("e2", responses[1].id)
    }
}
