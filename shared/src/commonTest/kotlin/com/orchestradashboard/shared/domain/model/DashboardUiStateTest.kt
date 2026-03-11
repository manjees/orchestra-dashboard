package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardUiStateTest {
    private val testAgents =
        listOf(
            Agent(id = "1", name = "Agent-1", type = Agent.AgentType.WORKER, status = Agent.AgentStatus.RUNNING, lastHeartbeat = 1000L),
            Agent(id = "2", name = "Agent-2", type = Agent.AgentType.ORCHESTRATOR, status = Agent.AgentStatus.IDLE, lastHeartbeat = 2000L),
            Agent(id = "3", name = "Agent-3", type = Agent.AgentType.REVIEWER, status = Agent.AgentStatus.ERROR, lastHeartbeat = 3000L),
            Agent(id = "4", name = "Agent-4", type = Agent.AgentType.PLANNER, status = Agent.AgentStatus.OFFLINE, lastHeartbeat = 4000L),
        )

    @Test
    fun `filteredAgents returns all agents when filter is null`() {
        val state = DashboardUiState(agents = testAgents, filter = null)
        assertEquals(4, state.filteredAgents.size)
    }

    @Test
    fun `filteredAgents returns only matching agents when filter is set`() {
        val state = DashboardUiState(agents = testAgents, filter = Agent.AgentStatus.RUNNING)
        assertEquals(1, state.filteredAgents.size)
        assertTrue(state.filteredAgents.all { it.status == Agent.AgentStatus.RUNNING })
    }

    @Test
    fun `filteredAgents returns empty list when no agents match filter`() {
        val agents =
            listOf(
                Agent(id = "1", name = "A", type = Agent.AgentType.WORKER, status = Agent.AgentStatus.RUNNING, lastHeartbeat = 1000L),
                Agent(id = "2", name = "B", type = Agent.AgentType.WORKER, status = Agent.AgentStatus.IDLE, lastHeartbeat = 2000L),
            )
        val state = DashboardUiState(agents = agents, filter = Agent.AgentStatus.ERROR)
        assertTrue(state.filteredAgents.isEmpty())
    }

    @Test
    fun `filteredAgents returns empty list when agents list is empty`() {
        val state = DashboardUiState(agents = emptyList(), filter = Agent.AgentStatus.RUNNING)
        assertTrue(state.filteredAgents.isEmpty())
    }

    @Test
    fun `selectedAgent is null by default`() {
        val state = DashboardUiState()
        assertNull(state.selectedAgent)
    }

    @Test
    fun `default state has expected initial values`() {
        val state = DashboardUiState()
        assertTrue(state.agents.isEmpty())
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertNull(state.filter)
        assertNull(state.selectedAgent)
    }
}
