package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgentCommandTest {
    @Test
    fun `should have TIMEOUT_MS constant equal to 30000`() {
        assertEquals(30_000L, AgentCommand.TIMEOUT_MS)
    }

    @Test
    fun `should have PENDING_TIMEOUT_MS constant equal to 60000`() {
        assertEquals(60_000L, AgentCommand.PENDING_TIMEOUT_MS)
    }

    @Test
    fun `CommandStatus contains all required states`() {
        val statuses = CommandStatus.entries.map { it.name }
        assertTrue("PENDING" in statuses)
        assertTrue("EXECUTING" in statuses)
        assertTrue("COMPLETED" in statuses)
        assertTrue("FAILED" in statuses)
    }

    @Test
    fun `CommandType contains all required types`() {
        val types = CommandType.entries.map { it.name }
        assertTrue("START" in types)
        assertTrue("STOP" in types)
        assertTrue("RESTART" in types)
    }

    @Test
    fun `AgentCommand default optional fields are null`() {
        val command =
            AgentCommand(
                id = "cmd-1",
                agentId = "agent-1",
                commandType = CommandType.STOP,
                status = CommandStatus.PENDING,
                requestedAt = 1000L,
                requestedBy = "user-1",
            )
        assertNull(command.executedAt)
        assertNull(command.completedAt)
        assertNull(command.failureReason)
    }
}
