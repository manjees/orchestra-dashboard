package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardUiStateTest {
    @Test
    fun `default state has null statusFilter`() {
        val state = DashboardUiState()
        assertNull(state.statusFilter)
    }

    @Test
    fun `copy with statusFilter preserves other fields`() {
        val agents =
            listOf(
                Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
            )
        val state =
            DashboardUiState(
                agents = agents,
                isLoading = true,
                error = "some error",
                connectionStatus = ConnectionStatus.CONNECTED,
            )

        val updated = state.copy(statusFilter = Agent.AgentStatus.RUNNING)

        assertEquals(agents, updated.agents)
        assertTrue(updated.isLoading)
        assertEquals("some error", updated.error)
        assertEquals(ConnectionStatus.CONNECTED, updated.connectionStatus)
        assertEquals(Agent.AgentStatus.RUNNING, updated.statusFilter)
    }

    @Test
    fun `filteredAgents returns all agents when statusFilter is null`() {
        val agents =
            listOf(
                Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
                Agent("2", "Beta", Agent.AgentType.PLANNER, Agent.AgentStatus.IDLE, 200L),
                Agent("3", "Gamma", Agent.AgentType.REVIEWER, Agent.AgentStatus.ERROR, 300L),
                Agent("4", "Delta", Agent.AgentType.ORCHESTRATOR, Agent.AgentStatus.OFFLINE, 400L),
            )
        val state = DashboardUiState(agents = agents, statusFilter = null)

        assertEquals(4, state.filteredAgents.size)
        assertEquals(agents, state.filteredAgents)
    }

    @Test
    fun `filteredAgents returns only matching agents when statusFilter is set`() {
        val agents =
            listOf(
                Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
                Agent("2", "Beta", Agent.AgentType.PLANNER, Agent.AgentStatus.IDLE, 200L),
                Agent("3", "Gamma", Agent.AgentType.REVIEWER, Agent.AgentStatus.ERROR, 300L),
                Agent("4", "Delta", Agent.AgentType.ORCHESTRATOR, Agent.AgentStatus.OFFLINE, 400L),
            )
        val state = DashboardUiState(agents = agents, statusFilter = Agent.AgentStatus.RUNNING)

        assertEquals(1, state.filteredAgents.size)
        assertEquals("1", state.filteredAgents.first().id)
    }

    @Test
    fun `filteredAgents returns empty list when no agents match filter`() {
        val agents =
            listOf(
                Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
            )
        val state = DashboardUiState(agents = agents, statusFilter = Agent.AgentStatus.ERROR)

        assertTrue(state.filteredAgents.isEmpty())
    }
}
