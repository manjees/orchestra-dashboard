package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class AgentJpaRepositoryTest {
    @Autowired
    lateinit var repository: AgentJpaRepository

    @Test
    fun `save and findById round-trips an agent`() {
        val entity =
            AgentEntity(
                id = "test-uuid-1",
                name = "Worker Alpha",
                type = "WORKER",
                status = "RUNNING",
                lastHeartbeat = 1700000000L,
                metadata = """{"env":"dev"}""",
            )

        repository.save(entity)
        val found = repository.findById("test-uuid-1")

        assertTrue(found.isPresent)
        assertEquals("Worker Alpha", found.get().name)
        assertEquals("WORKER", found.get().type)
        assertEquals("RUNNING", found.get().status)
        assertEquals(1700000000L, found.get().lastHeartbeat)
        assertEquals("""{"env":"dev"}""", found.get().metadata)
    }

    @Test
    fun `findByStatus returns matching agents only`() {
        repository.save(AgentEntity(id = "a1", name = "A1", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L))
        repository.save(AgentEntity(id = "a2", name = "A2", type = "WORKER", status = "RUNNING", lastHeartbeat = 200L))
        repository.save(AgentEntity(id = "a3", name = "A3", type = "WORKER", status = "IDLE", lastHeartbeat = 300L))

        val running = repository.findByStatus("RUNNING")

        assertEquals(2, running.size)
        assertTrue(running.all { it.status == "RUNNING" })
    }

    @Test
    fun `findByStatus returns empty list for no matches`() {
        repository.save(AgentEntity(id = "a1", name = "A1", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L))

        val result = repository.findByStatus("ERROR")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findByType returns matching agents only`() {
        repository.save(AgentEntity(id = "a1", name = "A1", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L))
        repository.save(AgentEntity(id = "a2", name = "A2", type = "PLANNER", status = "RUNNING", lastHeartbeat = 200L))
        repository.save(AgentEntity(id = "a3", name = "A3", type = "WORKER", status = "IDLE", lastHeartbeat = 300L))

        val workers = repository.findByType("WORKER")

        assertEquals(2, workers.size)
        assertTrue(workers.all { it.type == "WORKER" })
    }

    @Test
    fun `findByType returns empty list for no matches`() {
        repository.save(AgentEntity(id = "a1", name = "A1", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L))

        val result = repository.findByType("PLANNER")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `metadata persists as JSON column`() {
        val metadata = """{"key1":"value1","key2":"value2"}"""
        repository.save(AgentEntity(id = "a1", name = "A1", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L, metadata = metadata))

        val found = repository.findById("a1")

        assertTrue(found.isPresent)
        assertEquals(metadata, found.get().metadata)
    }
}
