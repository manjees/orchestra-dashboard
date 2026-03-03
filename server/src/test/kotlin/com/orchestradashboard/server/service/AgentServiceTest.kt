package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.CreateAgentRequest
import com.orchestradashboard.server.model.UpdateAgentStatusRequest
import com.orchestradashboard.server.repository.ServerAgentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery
import java.util.Optional
import java.util.function.Function

class AgentServiceTest {

    // ─── Fake Repository ────────────────────────────────────────

    private class FakeAgentRepository : ServerAgentRepository {
        val store = mutableMapOf<String, AgentEntity>()
        private var idSeq = 1L

        override fun findByAgentId(agentId: String): AgentEntity? = store[agentId]

        override fun findAllByStatus(status: String): List<AgentEntity> =
            store.values.filter { it.status == status }

        override fun findStaleAgents(threshold: Long): List<AgentEntity> =
            store.values.filter { it.lastHeartbeat < threshold && it.status != "OFFLINE" }

        override fun <S : AgentEntity> save(entity: S): S {
            val persisted = if (entity.id == 0L) {
                @Suppress("UNCHECKED_CAST")
                entity.copy(id = idSeq++) as S
            } else entity
            store[persisted.agentId] = persisted
            return persisted
        }

        override fun findAll(): List<AgentEntity> = store.values.toList()
        override fun findAll(sort: Sort): List<AgentEntity> = store.values.toList()
        override fun findAll(pageable: Pageable): Page<AgentEntity> = Page.empty()
        override fun findById(id: Long): Optional<AgentEntity> =
            store.values.find { it.id == id }.let { Optional.ofNullable(it) }
        override fun existsById(id: Long): Boolean = store.values.any { it.id == id }
        override fun count(): Long = store.size.toLong()
        override fun deleteById(id: Long) { store.entries.removeIf { it.value.id == id } }
        override fun delete(entity: AgentEntity) { store.remove(entity.agentId) }
        override fun deleteAllById(ids: Iterable<Long>) { ids.forEach { deleteById(it) } }
        override fun deleteAll(entities: Iterable<AgentEntity>) { entities.forEach { delete(it) } }
        override fun deleteAll() { store.clear() }
        override fun <S : AgentEntity> saveAll(entities: Iterable<S>): List<S> = entities.map { save(it) }
        override fun findAllById(ids: Iterable<Long>): List<AgentEntity> =
            ids.mapNotNull { id -> store.values.find { it.id == id } }
        override fun <S : AgentEntity> findAll(example: Example<S>): List<S> = emptyList()
        override fun <S : AgentEntity> findAll(example: Example<S>, sort: Sort): List<S> = emptyList()
        override fun <S : AgentEntity> findAll(example: Example<S>, pageable: Pageable): Page<S> = Page.empty()
        override fun <S : AgentEntity> findOne(example: Example<S>): Optional<S> = Optional.empty()
        override fun <S : AgentEntity> count(example: Example<S>): Long = 0
        override fun <S : AgentEntity> exists(example: Example<S>): Boolean = false
        override fun <S : AgentEntity, R> findBy(example: Example<S>, queryFunction: Function<FetchableFluentQuery<S>, R>): R =
            throw UnsupportedOperationException()
        override fun <S : AgentEntity> saveAndFlush(entity: S): S = save(entity)
        override fun <S : AgentEntity> saveAllAndFlush(entities: Iterable<S>): List<S> = saveAll(entities)
        override fun deleteAllInBatch(entities: Iterable<AgentEntity>) = deleteAll(entities)
        override fun deleteAllByIdInBatch(ids: Iterable<Long>) = deleteAllById(ids)
        override fun deleteAllInBatch() = deleteAll()
        override fun getOne(id: Long): AgentEntity = findById(id).orElseThrow()
        override fun getById(id: Long): AgentEntity = findById(id).orElseThrow()
        override fun getReferenceById(id: Long): AgentEntity = findById(id).orElseThrow()
        override fun flush() {}
    }

    // ─── Tests ─────────────────────────────────────────────────

    @Test
    fun `getAllAgents returns empty list when no agents are registered`() {
        val service = AgentService(FakeAgentRepository())
        assertTrue(service.getAllAgents().isEmpty())
    }

    @Test
    fun `registerAgent creates and returns a new agent`() {
        val repo = FakeAgentRepository()
        val service = AgentService(repo)
        val request = CreateAgentRequest(id = "agent-1", name = "Alpha", type = "WORKER")

        val result = service.registerAgent(request)

        assertEquals("agent-1", result.id)
        assertEquals("Alpha", result.name)
        assertEquals("OFFLINE", result.status)
        assertNotNull(repo.store["agent-1"])
    }

    @Test
    fun `registerAgent throws when agent ID already exists`() {
        val repo = FakeAgentRepository()
        repo.store["agent-1"] = AgentEntity(agentId = "agent-1", name = "Existing", type = "WORKER", status = "IDLE", lastHeartbeat = 0L)
        val service = AgentService(repo)

        assertThrows<IllegalArgumentException> {
            service.registerAgent(CreateAgentRequest("agent-1", "Duplicate", "WORKER"))
        }
    }

    @Test
    fun `getAgent returns null when agent not found`() {
        val service = AgentService(FakeAgentRepository())
        assertNull(service.getAgent("missing"))
    }

    @Test
    fun `updateAgentStatus updates status and heartbeat`() {
        val repo = FakeAgentRepository()
        repo.store["agent-1"] = AgentEntity(agentId = "agent-1", name = "Alpha", type = "WORKER", status = "OFFLINE", lastHeartbeat = 0L)
        val service = AgentService(repo)

        val result = service.updateAgentStatus("agent-1", UpdateAgentStatusRequest(status = "RUNNING", lastHeartbeat = 9999L))

        assertNotNull(result)
        assertEquals("RUNNING", result!!.status)
        assertEquals(9999L, result.lastHeartbeat)
    }

    @Test
    fun `deregisterAgent returns true and removes the agent`() {
        val repo = FakeAgentRepository()
        repo.store["agent-1"] = AgentEntity(agentId = "agent-1", name = "Alpha", type = "WORKER", status = "IDLE", lastHeartbeat = 0L)
        val service = AgentService(repo)

        val result = service.deregisterAgent("agent-1")

        assertTrue(result)
        assertNull(repo.store["agent-1"])
    }

    @Test
    fun `deregisterAgent returns false when agent not found`() {
        val service = AgentService(FakeAgentRepository())
        val result = service.deregisterAgent("missing")
        assertTrue(!result)
    }
}
