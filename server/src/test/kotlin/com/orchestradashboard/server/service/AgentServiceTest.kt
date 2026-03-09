package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.AgentMapper
import com.orchestradashboard.server.model.AgentRegistrationRequest
import com.orchestradashboard.server.repository.AgentJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class AgentServiceTest {
    private val repository: AgentJpaRepository = mock()
    private val mapper = AgentMapper()
    private val service = AgentService(repository, mapper)

    @Test
    fun `getAllAgents returns empty list when no agents`() {
        whenever(repository.findAll()).thenReturn(emptyList())

        val result = service.getAllAgents()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllAgents returns mapped responses`() {
        val entities =
            listOf(
                AgentEntity(id = "a1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L),
                AgentEntity(id = "a2", name = "Beta", type = "PLANNER", status = "IDLE", lastHeartbeat = 200L),
            )
        whenever(repository.findAll()).thenReturn(entities)

        val result = service.getAllAgents()

        assertEquals(2, result.size)
        assertEquals("a1", result[0].id)
        assertEquals("a2", result[1].id)
    }

    @Test
    fun `getAgent returns response when found`() {
        val entity = AgentEntity(id = "a1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L)
        whenever(repository.findById("a1")).thenReturn(Optional.of(entity))

        val result = service.getAgent("a1")

        assertEquals("a1", result.id)
        assertEquals("Alpha", result.name)
    }

    @Test
    fun `getAgent throws NoSuchElementException when not found`() {
        whenever(repository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.getAgent("missing")
        }
    }

    @Test
    fun `getAgentsByStatus delegates to repository`() {
        val entities =
            listOf(
                AgentEntity(id = "a1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L),
            )
        whenever(repository.findByStatus("RUNNING")).thenReturn(entities)

        val result = service.getAgentsByStatus("RUNNING")

        assertEquals(1, result.size)
        assertEquals("RUNNING", result[0].status)
        verify(repository).findByStatus("RUNNING")
    }

    @Test
    fun `registerAgent auto-assigns UUID when id is null`() {
        val request = AgentRegistrationRequest(name = "Alpha", type = "WORKER")
        val captor = argumentCaptor<AgentEntity>()
        whenever(repository.save(any<AgentEntity>())).thenAnswer { it.arguments[0] as AgentEntity }

        service.registerAgent(request)

        verify(repository).save(captor.capture())
        assertNotNull(captor.firstValue.id)
        assertTrue(captor.firstValue.id.isNotBlank())
    }

    @Test
    fun `registerAgent uses provided id when present`() {
        val request = AgentRegistrationRequest(id = "custom-id", name = "Alpha", type = "WORKER")
        val captor = argumentCaptor<AgentEntity>()
        whenever(repository.save(any<AgentEntity>())).thenAnswer { it.arguments[0] as AgentEntity }

        service.registerAgent(request)

        verify(repository).save(captor.capture())
        assertEquals("custom-id", captor.firstValue.id)
    }

    @Test
    fun `registerAgent returns created response`() {
        val request = AgentRegistrationRequest(id = "a1", name = "Alpha", type = "WORKER", metadata = mapOf("env" to "dev"))
        whenever(repository.save(any<AgentEntity>())).thenAnswer { it.arguments[0] as AgentEntity }

        val result = service.registerAgent(request)

        assertEquals("a1", result.id)
        assertEquals("Alpha", result.name)
        assertEquals("WORKER", result.type)
        assertEquals("OFFLINE", result.status)
        assertEquals(mapOf("env" to "dev"), result.metadata)
    }

    @Test
    fun `updateHeartbeat updates status and timestamp`() {
        val entity = AgentEntity(id = "a1", name = "Alpha", type = "WORKER", status = "OFFLINE", lastHeartbeat = 0L)
        whenever(repository.findById("a1")).thenReturn(Optional.of(entity))
        whenever(repository.save(any<AgentEntity>())).thenAnswer { it.arguments[0] as AgentEntity }

        val result = service.updateHeartbeat("a1", "RUNNING")

        assertEquals("RUNNING", result.status)
        assertTrue(result.lastHeartbeat > 0)
    }

    @Test
    fun `updateHeartbeat throws when agent not found`() {
        whenever(repository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.updateHeartbeat("missing", "RUNNING")
        }
    }
}
