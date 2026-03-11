package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgentDetailUiStateTest {
    @Test
    fun `default state has empty collections and loading false`() {
        val state = AgentDetailUiState()

        assertNull(state.agent)
        assertTrue(state.pipelineRuns.isEmpty())
        assertTrue(state.events.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.expandedPipelineIds.isEmpty())
    }

    @Test
    fun `selectedTabIndex defaults to 0`() {
        val state = AgentDetailUiState()

        assertEquals(0, state.selectedTabIndex)
    }

    @Test
    fun `copy preserves all fields correctly`() {
        val agent =
            Agent(
                id = "1",
                name = "Test",
                type = Agent.AgentType.WORKER,
                status = Agent.AgentStatus.RUNNING,
                lastHeartbeat = 100L,
            )
        val state =
            AgentDetailUiState(
                agent = agent,
                isLoading = true,
                selectedTabIndex = 2,
                expandedPipelineIds = setOf("p1"),
            )

        assertEquals(agent, state.agent)
        assertTrue(state.isLoading)
        assertEquals(2, state.selectedTabIndex)
        assertTrue(state.expandedPipelineIds.contains("p1"))
    }

    @Test
    fun `error field stores error message`() {
        val state = AgentDetailUiState(error = "Network error")

        assertEquals("Network error", state.error)
    }
}
