package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.AgentEventEntity
import com.orchestradashboard.server.model.AgentEventMapper
import com.orchestradashboard.server.model.CreateEventRequest
import com.orchestradashboard.server.repository.AgentEventJpaRepository
import com.orchestradashboard.server.repository.AgentJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import java.util.Optional

class EventServiceTest {
    private val eventRepository: AgentEventJpaRepository = mock()
    private val agentRepository: AgentJpaRepository = mock()
    private val eventMapper = AgentEventMapper()
    private val service = EventService(eventRepository, agentRepository, eventMapper)

    private val sampleEntity =
        AgentEventEntity(
            id = "evt-1",
            agentId = "agent-1",
            type = "STATUS_CHANGE",
            payload = """{"from":"IDLE","to":"RUNNING"}""",
            timestamp = 1700000000L,
        )

    @Test
    fun `getRecentEvents returns mapped responses with default limit`() {
        whenever(eventRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 20)))
            .thenReturn(listOf(sampleEntity))

        val result = service.getRecentEvents()

        assertEquals(1, result.size)
        assertEquals("evt-1", result[0].id)
        verify(eventRepository).findAllByOrderByTimestampDesc(PageRequest.of(0, 20))
    }

    @Test
    fun `getRecentEvents respects custom limit`() {
        whenever(eventRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 5)))
            .thenReturn(emptyList())

        service.getRecentEvents(limit = 5)

        verify(eventRepository).findAllByOrderByTimestampDesc(PageRequest.of(0, 5))
    }

    @Test
    fun `getRecentEvents caps limit at 100`() {
        whenever(eventRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 100)))
            .thenReturn(emptyList())

        service.getRecentEvents(limit = 200)

        verify(eventRepository).findAllByOrderByTimestampDesc(PageRequest.of(0, 100))
    }

    @Test
    fun `getEventsByAgentId returns filtered events`() {
        whenever(eventRepository.findByAgentIdOrderByTimestampDesc("agent-1", PageRequest.of(0, 20)))
            .thenReturn(listOf(sampleEntity))

        val result = service.getEventsByAgentId("agent-1")

        assertEquals(1, result.size)
        assertEquals("agent-1", result[0].agentId)
        verify(eventRepository).findByAgentIdOrderByTimestampDesc("agent-1", PageRequest.of(0, 20))
    }

    @Test
    fun `getEventsByAgentId returns empty list for unknown agent`() {
        whenever(eventRepository.findByAgentIdOrderByTimestampDesc("unknown", PageRequest.of(0, 20)))
            .thenReturn(emptyList())

        val result = service.getEventsByAgentId("unknown")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `createEvent saves entity and returns response`() {
        val agent = AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L)
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent))
        whenever(eventRepository.save(any<AgentEventEntity>())).thenAnswer { it.arguments[0] as AgentEventEntity }

        val request = CreateEventRequest(agentId = "agent-1", type = "STATUS_CHANGE", payload = mapOf("from" to "IDLE"))
        val result = service.createEvent(request)

        assertEquals("agent-1", result.agentId)
        assertEquals("STATUS_CHANGE", result.type)
        assertEquals(mapOf("from" to "IDLE"), result.payload)
    }

    @Test
    fun `createEvent throws NoSuchElementException for invalid agentId`() {
        whenever(agentRepository.findById("invalid")).thenReturn(Optional.empty())

        val request = CreateEventRequest(agentId = "invalid", type = "STATUS_CHANGE")

        assertThrows<NoSuchElementException> {
            service.createEvent(request)
        }
    }

    @Test
    fun `createEvent auto-generates id and timestamp`() {
        val agent = AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L)
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent))
        val captor = argumentCaptor<AgentEventEntity>()
        whenever(eventRepository.save(any<AgentEventEntity>())).thenAnswer { it.arguments[0] as AgentEventEntity }

        val request = CreateEventRequest(agentId = "agent-1", type = "TASK_COMPLETED")
        service.createEvent(request)

        verify(eventRepository).save(captor.capture())
        assertTrue(captor.firstValue.id.isNotBlank())
        assertTrue(captor.firstValue.timestamp > 0)
    }
}
