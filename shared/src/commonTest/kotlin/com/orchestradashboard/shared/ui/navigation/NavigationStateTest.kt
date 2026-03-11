package com.orchestradashboard.shared.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class NavigationStateTest {
    @Test
    fun `Dashboard is a valid navigation state`() {
        val state: NavigationState = NavigationState.Dashboard

        assertIs<NavigationState.Dashboard>(state)
    }

    @Test
    fun `AgentDetail stores agent id`() {
        val state = NavigationState.AgentDetail("agent-1")

        assertEquals("agent-1", state.agentId)
    }

    @Test
    fun `AgentDetail rejects blank agent id`() {
        assertFailsWith<IllegalArgumentException> {
            NavigationState.AgentDetail("")
        }
    }

    @Test
    fun `AgentDetail rejects whitespace-only agent id`() {
        assertFailsWith<IllegalArgumentException> {
            NavigationState.AgentDetail("   ")
        }
    }
}
