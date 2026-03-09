package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEventEntity
import com.orchestradashboard.server.model.AgentEventMapper
import com.orchestradashboard.server.model.CreateEventRequest
import com.orchestradashboard.server.repository.AgentEventJpaRepository
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

class EventServiceTest {
    private val eventRepository: AgentEventJpaRepository = mock()
    private val agentRepository: AgentJpaRepository = mock()
    private val mapper = AgentEventMapper()
    private val service = EventService(eventRepository, agentRepository, mapper)

    private val defaultEntity =
        AgentEventEntity(
            id = "evt-1",
            agentId = "agent-1",
            type = "STATUS_CHANGE",
            payload = "{}",
            timestamp = 1700000000L,
        )

    @Test
    fun `getRecentEvents returns mapped responses`() {
        whenever(eventRepository.findTop50ByOrderByTimestampDesc()).thenReturn(listOf(defaultEntity))

        val result = service.getRecentEvents(null, 50)

        assertEquals(1, result.size)
        assertEquals("evt-1", result[0].id)
    }

    @Test
    fun `getRecentEvents respects limit parameter`() {
        val entities =
            (1..10).map {
                AgentEventEntity(id = "evt-$it", agentId = "agent-1", type = "HEARTBEAT", payload = "", timestamp = it.toLong())
            }
        whenever(eventRepository.findTop50ByOrderByTimestampDesc()).thenReturn(entities)

        val result = service.getRecentEvents(null, 3)

        assertEquals(3, result.size)
    }

    @Test
    fun `getEventsByAgentId returns filtered events`() {
        whenever(eventRepository.findByAgentIdOrderByTimestampDesc("agent-1")).thenReturn(listOf(defaultEntity))

        val result = service.getRecentEvents("agent-1", 50)

        assertEquals(1, result.size)
        verify(eventRepository).findByAgentIdOrderByTimestampDesc("agent-1")
    }

    @Test
    fun `createEvent saves entity and returns response`() {
        val request = CreateEventRequest(agentId = "agent-1", type = "STATUS_CHANGE", payload = "{}")
        whenever(agentRepository.existsById("agent-1")).thenReturn(true)
        whenever(eventRepository.save(any<AgentEventEntity>())).thenAnswer { it.arguments[0] as AgentEventEntity }

        val result = service.createEvent(request)

        assertEquals("agent-1", result.agentId)
        assertEquals("STATUS_CHANGE", result.type)
        assertEquals("{}", result.payload)
    }

    @Test
    fun `createEvent validates agent exists`() {
        val request = CreateEventRequest(agentId = "missing", type = "ERROR")
        whenever(agentRepository.existsById("missing")).thenReturn(false)

        assertThrows<NoSuchElementException> {
            service.createEvent(request)
        }
    }

    @Test
    fun `createEvent auto-assigns UUID and timestamp`() {
        val request = CreateEventRequest(agentId = "agent-1", type = "HEARTBEAT")
        whenever(agentRepository.existsById("agent-1")).thenReturn(true)
        val captor = argumentCaptor<AgentEventEntity>()
        whenever(eventRepository.save(any<AgentEventEntity>())).thenAnswer { it.arguments[0] as AgentEventEntity }

        service.createEvent(request)

        verify(eventRepository).save(captor.capture())
        assertNotNull(captor.firstValue.id)
        assertTrue(captor.firstValue.id.isNotBlank())
        assertTrue(captor.firstValue.timestamp > 0)
    }
}
