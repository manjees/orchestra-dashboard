package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgentTest {

    @Test
    fun `should format displayName as name with lowercase type`() {
        val agent = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.ORCHESTRATOR,
            status = Agent.AgentStatus.RUNNING,
            lastHeartbeat = 1000L
        )
        assertEquals("Alpha (orchestrator)", agent.displayName)
    }

    @Test
    fun `should format displayName for worker type`() {
        val agent = Agent(
            id = "agent-2",
            name = "Beta",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.IDLE,
            lastHeartbeat = 2000L
        )
        assertEquals("Beta (worker)", agent.displayName)
    }

    @Test
    fun `should format displayName for reviewer type`() {
        val agent = Agent(
            id = "agent-3",
            name = "Gamma",
            type = Agent.AgentType.REVIEWER,
            status = Agent.AgentStatus.IDLE,
            lastHeartbeat = 3000L
        )
        assertEquals("Gamma (reviewer)", agent.displayName)
    }

    @Test
    fun `should format displayName for planner type`() {
        val agent = Agent(
            id = "agent-4",
            name = "Delta",
            type = Agent.AgentType.PLANNER,
            status = Agent.AgentStatus.IDLE,
            lastHeartbeat = 4000L
        )
        assertEquals("Delta (planner)", agent.displayName)
    }

    @Test
    fun `should return isHealthy true when status is RUNNING`() {
        val agent = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.RUNNING,
            lastHeartbeat = 1000L
        )
        assertTrue(agent.isHealthy)
    }

    @Test
    fun `should return isHealthy true when status is IDLE`() {
        val agent = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.IDLE,
            lastHeartbeat = 1000L
        )
        assertTrue(agent.isHealthy)
    }

    @Test
    fun `should return isHealthy false when status is ERROR`() {
        val agent = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.ERROR,
            lastHeartbeat = 1000L
        )
        assertFalse(agent.isHealthy)
    }

    @Test
    fun `should return isHealthy false when status is OFFLINE`() {
        val agent = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.OFFLINE,
            lastHeartbeat = 1000L
        )
        assertFalse(agent.isHealthy)
    }

    @Test
    fun `should default metadata to empty map`() {
        val agent = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.RUNNING,
            lastHeartbeat = 1000L
        )
        assertEquals(emptyMap(), agent.metadata)
    }

    @Test
    fun `should support equality for identical agents`() {
        val agent1 = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.ORCHESTRATOR,
            status = Agent.AgentStatus.RUNNING,
            lastHeartbeat = 1000L,
            metadata = mapOf("key" to "value")
        )
        val agent2 = Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.ORCHESTRATOR,
            status = Agent.AgentStatus.RUNNING,
            lastHeartbeat = 1000L,
            metadata = mapOf("key" to "value")
        )
        assertEquals(agent1, agent2)
    }
}
