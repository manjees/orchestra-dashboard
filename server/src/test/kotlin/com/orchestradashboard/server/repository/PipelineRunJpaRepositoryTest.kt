package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.PipelineRunEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class PipelineRunJpaRepositoryTest {
    @Autowired
    lateinit var pipelineRepository: PipelineRunJpaRepository

    @Autowired
    lateinit var agentRepository: AgentJpaRepository

    @BeforeEach
    fun setUp() {
        agentRepository.save(AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L))
        agentRepository.save(AgentEntity(id = "agent-2", name = "Beta", type = "PLANNER", status = "IDLE", lastHeartbeat = 200L))
    }

    @Test
    fun `save and findById round-trips a pipeline run`() {
        val entity =
            PipelineRunEntity(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "CI Pipeline",
                status = "RUNNING",
                steps = """[{"name":"Build","status":"PASSED","detail":"OK","elapsed_ms":1200}]""",
                startedAt = 1700000000L,
                finishedAt = null,
                triggerInfo = "manual",
            )

        pipelineRepository.save(entity)
        val found = pipelineRepository.findById("run-1")

        assertTrue(found.isPresent)
        assertEquals("CI Pipeline", found.get().pipelineName)
        assertEquals("RUNNING", found.get().status)
        assertEquals("agent-1", found.get().agentId)
    }

    @Test
    fun `steps column persists JSON correctly`() {
        val stepsJson = """[{"name":"Build","status":"PASSED","detail":"OK","elapsed_ms":500}]"""
        pipelineRepository.save(
            PipelineRunEntity(id = "run-1", agentId = "agent-1", pipelineName = "P", steps = stepsJson, startedAt = 100L),
        )

        val found = pipelineRepository.findById("run-1")

        assertTrue(found.isPresent)
        assertEquals(stepsJson, found.get().steps)
    }

    @Test
    fun `findByAgentId returns matching runs only`() {
        pipelineRepository.save(PipelineRunEntity(id = "r1", agentId = "agent-1", pipelineName = "P1", startedAt = 100L))
        pipelineRepository.save(PipelineRunEntity(id = "r2", agentId = "agent-1", pipelineName = "P2", startedAt = 200L))
        pipelineRepository.save(PipelineRunEntity(id = "r3", agentId = "agent-2", pipelineName = "P3", startedAt = 300L))

        val page = pipelineRepository.findByAgentId("agent-1", PageRequest.of(0, 20))

        assertEquals(2, page.content.size)
        assertTrue(page.content.all { it.agentId == "agent-1" })
    }

    @Test
    fun `findByAgentId supports pagination`() {
        pipelineRepository.save(PipelineRunEntity(id = "r1", agentId = "agent-1", pipelineName = "P1", startedAt = 100L))
        pipelineRepository.save(PipelineRunEntity(id = "r2", agentId = "agent-1", pipelineName = "P2", startedAt = 200L))
        pipelineRepository.save(PipelineRunEntity(id = "r3", agentId = "agent-1", pipelineName = "P3", startedAt = 300L))

        val page = pipelineRepository.findByAgentId("agent-1", PageRequest.of(0, 2))

        assertEquals(2, page.content.size)
        assertEquals(3, page.totalElements)
    }

    @Test
    fun `findByStatus returns matching runs only`() {
        pipelineRepository.save(
            PipelineRunEntity(id = "r1", agentId = "agent-1", pipelineName = "P1", status = "RUNNING", startedAt = 100L),
        )
        pipelineRepository.save(PipelineRunEntity(id = "r2", agentId = "agent-1", pipelineName = "P2", status = "PASSED", startedAt = 200L))
        pipelineRepository.save(
            PipelineRunEntity(id = "r3", agentId = "agent-2", pipelineName = "P3", status = "RUNNING", startedAt = 300L),
        )

        val page = pipelineRepository.findByStatus("RUNNING", PageRequest.of(0, 20))

        assertEquals(2, page.content.size)
        assertTrue(page.content.all { it.status == "RUNNING" })
    }

    @Test
    fun `findByAgentIdAndStatus returns intersection`() {
        pipelineRepository.save(
            PipelineRunEntity(id = "r1", agentId = "agent-1", pipelineName = "P1", status = "RUNNING", startedAt = 100L),
        )
        pipelineRepository.save(PipelineRunEntity(id = "r2", agentId = "agent-1", pipelineName = "P2", status = "PASSED", startedAt = 200L))
        pipelineRepository.save(
            PipelineRunEntity(id = "r3", agentId = "agent-2", pipelineName = "P3", status = "RUNNING", startedAt = 300L),
        )

        val page = pipelineRepository.findByAgentIdAndStatus("agent-1", "RUNNING", PageRequest.of(0, 20))

        assertEquals(1, page.content.size)
        assertEquals("r1", page.content[0].id)
    }

    @Test
    fun `findByAgentId returns empty page for unknown agent`() {
        val page = pipelineRepository.findByAgentId("unknown", PageRequest.of(0, 20))

        assertTrue(page.content.isEmpty())
    }

    @Test
    fun `findAll with Pageable returns correct page`() {
        pipelineRepository.save(PipelineRunEntity(id = "r1", agentId = "agent-1", pipelineName = "P1", startedAt = 100L))
        pipelineRepository.save(PipelineRunEntity(id = "r2", agentId = "agent-1", pipelineName = "P2", startedAt = 200L))
        pipelineRepository.save(PipelineRunEntity(id = "r3", agentId = "agent-2", pipelineName = "P3", startedAt = 300L))

        val page = pipelineRepository.findAll(PageRequest.of(0, 2))

        assertEquals(2, page.content.size)
        assertEquals(3, page.totalElements)
    }
}
