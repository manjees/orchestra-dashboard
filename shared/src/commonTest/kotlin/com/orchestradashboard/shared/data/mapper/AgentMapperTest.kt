package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.domain.model.Agent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgentMapperTest {

    private val mapper = AgentMapper()

    @Test
    fun `toDomain maps valid DTO with ORCHESTRATOR type correctly`() {
        val dto = AgentDto(
            id = "agent-1",
            name = "Main Orchestrator",
            type = "ORCHESTRATOR",
            status = "RUNNING",
            lastHeartbeat = 1700000000L
        )

        val result = mapper.toDomain(dto)

        assertEquals("agent-1", result.id)
        assertEquals("Main Orchestrator", result.name)
        assertEquals(Agent.AgentType.ORCHESTRATOR, result.type)
        assertEquals(Agent.AgentStatus.RUNNING, result.status)
        assertEquals(1700000000L, result.lastHeartbeat)
    }

    @Test
    fun `toDomain maps valid DTO with WORKER type correctly`() {
        val dto = AgentDto(
            id = "worker-42",
            name = "Worker Alpha",
            type = "WORKER",
            status = "IDLE",
            lastHeartbeat = 1700000001L
        )

        val result = mapper.toDomain(dto)

        assertEquals(Agent.AgentType.WORKER, result.type)
        assertEquals(Agent.AgentStatus.IDLE, result.status)
        assertTrue(result.isHealthy)
    }

    @Test
    fun `toDomain handles unknown type by defaulting to WORKER`() {
        val dto = AgentDto(
            id = "x",
            name = "Unknown",
            type = "INVALID_TYPE",
            status = "RUNNING",
            lastHeartbeat = 0L
        )

        val result = mapper.toDomain(dto)

        assertEquals(Agent.AgentType.WORKER, result.type)
    }

    @Test
    fun `toDomain handles unknown status by defaulting to OFFLINE`() {
        val dto = AgentDto(
            id = "x",
            name = "Unknown",
            type = "WORKER",
            status = "UNKNOWN_STATUS",
            lastHeartbeat = 0L
        )

        val result = mapper.toDomain(dto)

        assertEquals(Agent.AgentStatus.OFFLINE, result.status)
        assertFalse(result.isHealthy)
    }

    @Test
    fun `toDomain handles case-insensitive type parsing`() {
        val dto = AgentDto(
            id = "p",
            name = "Planner",
            type = "planner",
            status = "running",
            lastHeartbeat = 0L
        )

        val result = mapper.toDomain(dto)

        assertEquals(Agent.AgentType.PLANNER, result.type)
        assertEquals(Agent.AgentStatus.RUNNING, result.status)
    }

    @Test
    fun `toDomain maps metadata correctly`() {
        val dto = AgentDto(
            id = "m",
            name = "Meta Agent",
            type = "WORKER",
            status = "IDLE",
            lastHeartbeat = 0L,
            metadata = mapOf("region" to "us-east-1", "version" to "2.1")
        )

        val result = mapper.toDomain(dto)

        assertEquals("us-east-1", result.metadata["region"])
        assertEquals("2.1", result.metadata["version"])
    }

    @Test
    fun `toDomain maps empty metadata to empty map`() {
        val dto = AgentDto(
            id = "e",
            name = "Empty",
            type = "WORKER",
            status = "IDLE",
            lastHeartbeat = 0L
        )

        val result = mapper.toDomain(dto)

        assertTrue(result.metadata.isEmpty())
    }

    @Test
    fun `toDomain list variant maps all elements`() {
        val dtos = listOf(
            AgentDto("1", "Agent One", "WORKER", "RUNNING", 100L),
            AgentDto("2", "Agent Two", "PLANNER", "IDLE", 200L),
            AgentDto("3", "Agent Three", "REVIEWER", "ERROR", 300L)
        )

        val results = mapper.toDomain(dtos)

        assertEquals(3, results.size)
        assertEquals("1", results[0].id)
        assertEquals("2", results[1].id)
        assertEquals("3", results[2].id)
    }

    @Test
    fun `toDomain list variant maps empty list`() {
        val results = mapper.toDomain(emptyList())
        assertTrue(results.isEmpty())
    }

    @Test
    fun `agent displayName formats correctly`() {
        val dto = AgentDto("a", "Reviewer Bot", "REVIEWER", "RUNNING", 0L)
        val agent = mapper.toDomain(dto)
        assertEquals("Reviewer Bot (reviewer)", agent.displayName)
    }

    @Test
    fun `isHealthy is false for ERROR status`() {
        val dto = AgentDto("e", "Broken", "WORKER", "ERROR", 0L)
        val agent = mapper.toDomain(dto)
        assertFalse(agent.isHealthy)
    }
}
