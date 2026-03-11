package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardUiStateTest {
    private val agents =
        listOf(
            Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
            Agent("2", "Beta", Agent.AgentType.PLANNER, Agent.AgentStatus.IDLE, 200L),
            Agent("3", "Gamma", Agent.AgentType.REVIEWER, Agent.AgentStatus.ERROR, 300L),
        )

    @Test
    fun `default state has empty agents and no filter`() {
        val state = DashboardUiState()

        assertTrue(state.agents.isEmpty())
        assertNull(state.filter)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertNull(state.selectedAgent)
    }

    @Test
    fun `filteredAgents returns all agents when filter is null`() {
        val state = DashboardUiState(agents = agents, filter = null)

        assertEquals(3, state.filteredAgents.size)
        assertEquals(agents, state.filteredAgents)
    }

    @Test
    fun `filteredAgents returns only matching agents when filter is set`() {
        val state = DashboardUiState(agents = agents, filter = Agent.AgentStatus.RUNNING)

        assertEquals(1, state.filteredAgents.size)
        assertEquals("1", state.filteredAgents[0].id)
    }

    @Test
    fun `filteredAgents returns empty list when no agents match filter`() {
        val state = DashboardUiState(agents = agents, filter = Agent.AgentStatus.OFFLINE)

        assertTrue(state.filteredAgents.isEmpty())
    }

    @Test
    fun `selectedAgent can be set and cleared via copy`() {
        val state = DashboardUiState(agents = agents)

        val withSelected = state.copy(selectedAgent = agents[0])
        assertEquals(agents[0], withSelected.selectedAgent)

        val cleared = withSelected.copy(selectedAgent = null)
        assertNull(cleared.selectedAgent)
    }
}
