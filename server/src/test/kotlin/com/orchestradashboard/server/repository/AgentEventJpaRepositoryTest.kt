package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.AgentEventEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class AgentEventJpaRepositoryTest {
    @Autowired
    lateinit var eventRepository: AgentEventJpaRepository

    @Autowired
    lateinit var agentRepository: AgentJpaRepository

    @BeforeEach
    fun setUp() {
        eventRepository.deleteAll()
        agentRepository.deleteAll()
        agentRepository.save(AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L))
    }

    @Test
    fun `save and findById round-trips an event`() {
        val entity =
            AgentEventEntity(
                id = "evt-1",
                agentId = "agent-1",
                type = "STATUS_CHANGE",
                payload = """{"from":"IDLE","to":"RUNNING"}""",
                timestamp = 1700000000L,
            )

        eventRepository.save(entity)
        val found = eventRepository.findById("evt-1")

        assertTrue(found.isPresent)
        assertEquals("agent-1", found.get().agentId)
        assertEquals("STATUS_CHANGE", found.get().type)
        assertEquals("""{"from":"IDLE","to":"RUNNING"}""", found.get().payload)
        assertEquals(1700000000L, found.get().timestamp)
    }

    @Test
    fun `findByAgentIdOrderByTimestampDesc returns events for specific agent in descending order`() {
        agentRepository.save(AgentEntity(id = "agent-2", name = "Beta", type = "PLANNER", status = "IDLE", lastHeartbeat = 200L))
        eventRepository.save(AgentEventEntity(id = "e1", agentId = "agent-1", type = "HEARTBEAT", timestamp = 1000L))
        eventRepository.save(AgentEventEntity(id = "e2", agentId = "agent-1", type = "STATUS_CHANGE", timestamp = 3000L))
        eventRepository.save(AgentEventEntity(id = "e3", agentId = "agent-1", type = "ERROR", timestamp = 2000L))
        eventRepository.save(AgentEventEntity(id = "e4", agentId = "agent-2", type = "HEARTBEAT", timestamp = 2500L))

        val result = eventRepository.findByAgentIdOrderByTimestampDesc("agent-1", PageRequest.of(0, 10))

        assertEquals(3, result.size)
        assertEquals("e2", result[0].id)
        assertEquals(3000L, result[0].timestamp)
        assertEquals("e3", result[1].id)
        assertEquals(2000L, result[1].timestamp)
        assertEquals("e1", result[2].id)
        assertEquals(1000L, result[2].timestamp)
    }

    @Test
    fun `findByAgentIdOrderByTimestampDesc returns empty list for unknown agent`() {
        eventRepository.save(AgentEventEntity(id = "e1", agentId = "agent-1", type = "HEARTBEAT", timestamp = 1000L))

        val result = eventRepository.findByAgentIdOrderByTimestampDesc("unknown-agent", PageRequest.of(0, 10))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findByAgentIdOrderByTimestampDesc respects pageable limit`() {
        for (i in 1..5) {
            eventRepository.save(AgentEventEntity(id = "e$i", agentId = "agent-1", type = "HEARTBEAT", timestamp = i.toLong()))
        }

        val result = eventRepository.findByAgentIdOrderByTimestampDesc("agent-1", PageRequest.of(0, 2))

        assertEquals(2, result.size)
        assertEquals(5L, result[0].timestamp)
        assertEquals(4L, result[1].timestamp)
    }

    @Test
    fun `findAllByOrderByTimestampDesc returns all events in descending order`() {
        agentRepository.save(AgentEntity(id = "agent-2", name = "Beta", type = "PLANNER", status = "IDLE", lastHeartbeat = 200L))
        eventRepository.save(AgentEventEntity(id = "e1", agentId = "agent-1", type = "HEARTBEAT", timestamp = 1000L))
        eventRepository.save(AgentEventEntity(id = "e2", agentId = "agent-2", type = "STATUS_CHANGE", timestamp = 3000L))
        eventRepository.save(AgentEventEntity(id = "e3", agentId = "agent-1", type = "ERROR", timestamp = 2000L))

        val result = eventRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 10))

        assertEquals(3, result.size)
        assertEquals("e2", result[0].id)
        assertEquals("e3", result[1].id)
        assertEquals("e1", result[2].id)
    }

    @Test
    fun `findTop50ByOrderByTimestampDesc returns events in descending order`() {
        agentRepository.save(AgentEntity(id = "agent-2", name = "Beta", type = "PLANNER", status = "IDLE", lastHeartbeat = 200L))
        eventRepository.save(AgentEventEntity(id = "e1", agentId = "agent-1", type = "HEARTBEAT", timestamp = 1000L))
        eventRepository.save(AgentEventEntity(id = "e2", agentId = "agent-1", type = "STATUS_CHANGE", timestamp = 3000L))
        eventRepository.save(AgentEventEntity(id = "e3", agentId = "agent-2", type = "ERROR", timestamp = 2000L))

        val result = eventRepository.findTop50ByOrderByTimestampDesc()

        assertEquals(3, result.size)
        assertEquals("e2", result[0].id)
        assertEquals("e3", result[1].id)
        assertEquals("e1", result[2].id)
    }

    @Test
    fun `payload persists as TEXT column with JSON content`() {
        val complexPayload = """{"from":"IDLE","to":"RUNNING","issue_num":"42","nested":{"key":"value"}}"""
        eventRepository.save(
            AgentEventEntity(id = "e1", agentId = "agent-1", type = "STATUS_CHANGE", payload = complexPayload, timestamp = 1000L),
        )

        val found = eventRepository.findById("e1")

        assertTrue(found.isPresent)
        assertEquals(complexPayload, found.get().payload)
    }

    @Test
    fun `foreign key enforced - save fails with nonexistent agentId`() {
        val entity = AgentEventEntity(id = "e1", agentId = "nonexistent", type = "HEARTBEAT", timestamp = 1000L)
        eventRepository.save(entity)

        assertThrows<Exception> {
            eventRepository.flush()
        }
    }
}
