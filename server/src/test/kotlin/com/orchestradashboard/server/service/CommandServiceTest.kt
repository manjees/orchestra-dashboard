package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentCommandEntity
import com.orchestradashboard.server.model.AgentCommandMapper
import com.orchestradashboard.server.model.AgentCommandResponse
import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.CreateCommandRequest
import com.orchestradashboard.server.repository.AgentCommandJpaRepository
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.websocket.AgentEventWebSocketHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class CommandServiceTest {
    private val commandRepository: AgentCommandJpaRepository = mock()
    private val agentRepository: AgentJpaRepository = mock()
    private val commandMapper = AgentCommandMapper()
    private val webSocketHandler: AgentEventWebSocketHandler = mock()
    private val service = CommandService(commandRepository, agentRepository, commandMapper, webSocketHandler)

    private val sampleAgent =
        AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L)

    @Test
    fun `should persist command execution status to audit table`() {
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(sampleAgent))
        whenever(commandRepository.findByAgentIdAndStatusIn("agent-1", listOf("PENDING", "EXECUTING")))
            .thenReturn(emptyList())
        val captor = argumentCaptor<AgentCommandEntity>()
        whenever(commandRepository.save(any<AgentCommandEntity>())).thenAnswer { it.arguments[0] as AgentCommandEntity }

        val request = CreateCommandRequest(agentId = "agent-1", commandType = "STOP")
        val result = service.createCommand(request, "dashboard-client")

        verify(commandRepository).save(captor.capture())
        assertEquals("agent-1", captor.firstValue.agentId)
        assertEquals("STOP", captor.firstValue.commandType)
        assertEquals("PENDING", captor.firstValue.status)
        assertEquals("dashboard-client", captor.firstValue.requestedBy)
        assertTrue(captor.firstValue.id.isNotBlank())
        assertTrue(captor.firstValue.requestedAt > 0)
        assertEquals("PENDING", result.status)
    }

    @Test
    fun `should broadcast command to WebSocket after creation`() {
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(sampleAgent))
        whenever(commandRepository.findByAgentIdAndStatusIn("agent-1", listOf("PENDING", "EXECUTING")))
            .thenReturn(emptyList())
        whenever(commandRepository.save(any<AgentCommandEntity>())).thenAnswer { it.arguments[0] as AgentCommandEntity }

        val request = CreateCommandRequest(agentId = "agent-1", commandType = "STOP")
        service.createCommand(request, "dashboard-client")

        val captor = argumentCaptor<AgentCommandResponse>()
        verify(webSocketHandler).broadcastCommand(captor.capture())
        assertEquals("agent-1", captor.firstValue.agentId)
        assertEquals("STOP", captor.firstValue.commandType)
    }

    @Test
    fun `should reject command when active PENDING command exists for same agent`() {
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(sampleAgent))
        val pendingCommand =
            AgentCommandEntity(
                agentId = "agent-1",
                commandType = "START",
                status = "PENDING",
                requestedAt = 1000L,
                requestedBy = "user",
            )
        whenever(commandRepository.findByAgentIdAndStatusIn("agent-1", listOf("PENDING", "EXECUTING")))
            .thenReturn(listOf(pendingCommand))

        val request = CreateCommandRequest(agentId = "agent-1", commandType = "STOP")

        assertThrows<IllegalStateException> {
            service.createCommand(request, "dashboard-client")
        }
    }

    @Test
    fun `should reject command when active EXECUTING command exists for same agent`() {
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(sampleAgent))
        val executingCommand =
            AgentCommandEntity(
                agentId = "agent-1",
                commandType = "START",
                status = "EXECUTING",
                requestedAt = 1000L,
                requestedBy = "user",
                executedAt = 2000L,
            )
        whenever(commandRepository.findByAgentIdAndStatusIn("agent-1", listOf("PENDING", "EXECUTING")))
            .thenReturn(listOf(executingCommand))

        val request = CreateCommandRequest(agentId = "agent-1", commandType = "STOP")

        assertThrows<IllegalStateException> {
            service.createCommand(request, "dashboard-client")
        }
    }

    @Test
    fun `should handle timeout if agent does not respond within 30s`() {
        val now = System.currentTimeMillis()
        val staleExecuting =
            AgentCommandEntity(
                id = "cmd-stale",
                agentId = "agent-1",
                commandType = "STOP",
                status = "EXECUTING",
                requestedAt = now - 40_000L,
                requestedBy = "user",
                executedAt = now - 35_000L,
            )
        whenever(commandRepository.findByStatusAndExecutedAtLessThan(any(), any()))
            .thenReturn(listOf(staleExecuting))
        whenever(commandRepository.findByStatusAndRequestedAtLessThan(any(), any()))
            .thenReturn(emptyList())
        whenever(commandRepository.save(any<AgentCommandEntity>())).thenAnswer { it.arguments[0] as AgentCommandEntity }

        service.timeoutStaleCommands()

        val captor = argumentCaptor<AgentCommandEntity>()
        verify(commandRepository).save(captor.capture())
        assertEquals("FAILED", captor.firstValue.status)
        assertEquals("timeout", captor.firstValue.failureReason)

        val responseCaptor = argumentCaptor<AgentCommandResponse>()
        verify(webSocketHandler).broadcastCommand(responseCaptor.capture())
        assertEquals("FAILED", responseCaptor.firstValue.status)
    }

    @Test
    fun `should timeout PENDING commands after 60s`() {
        val now = System.currentTimeMillis()
        val stalePending =
            AgentCommandEntity(
                id = "cmd-pending",
                agentId = "agent-1",
                commandType = "RESTART",
                status = "PENDING",
                requestedAt = now - 70_000L,
                requestedBy = "user",
            )
        whenever(commandRepository.findByStatusAndExecutedAtLessThan(any(), any()))
            .thenReturn(emptyList())
        whenever(commandRepository.findByStatusAndRequestedAtLessThan(any(), any()))
            .thenReturn(listOf(stalePending))
        whenever(commandRepository.save(any<AgentCommandEntity>())).thenAnswer { it.arguments[0] as AgentCommandEntity }

        service.timeoutStaleCommands()

        val captor = argumentCaptor<AgentCommandEntity>()
        verify(commandRepository).save(captor.capture())
        assertEquals("FAILED", captor.firstValue.status)
        assertEquals("pending_timeout", captor.firstValue.failureReason)
    }

    @Test
    fun `should validate command type`() {
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(sampleAgent))

        val request = CreateCommandRequest(agentId = "agent-1", commandType = "INVALID")

        assertThrows<IllegalArgumentException> {
            service.createCommand(request, "dashboard-client")
        }
    }

    @Test
    fun `should throw NoSuchElementException for nonexistent agent`() {
        whenever(agentRepository.findById("nonexistent")).thenReturn(Optional.empty())

        val request = CreateCommandRequest(agentId = "nonexistent", commandType = "STOP")

        assertThrows<NoSuchElementException> {
            service.createCommand(request, "dashboard-client")
        }
    }
}
