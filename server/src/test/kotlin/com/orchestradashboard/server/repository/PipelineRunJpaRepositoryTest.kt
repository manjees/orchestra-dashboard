package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.PipelineRunEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        pipelineRepository.deleteAll()
        agentRepository.deleteAll()
        agentRepository.save(AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L))
        agentRepository.save(AgentEntity(id = "agent-2", name = "Beta", type = "PLANNER", status = "IDLE", lastHeartbeat = 200L))
    }

    @Test
    fun `save and findById round-trips a pipeline run`() {
        val entity =
            PipelineRunEntity(
                id = "pipe-1",
                agentId = "agent-1",
                pipelineName = "build-deploy",
                status = "RUNNING",
                startedAt = 1700000000L,
                triggerInfo = "manual",
            )

        pipelineRepository.save(entity)
        val found = pipelineRepository.findById("pipe-1")

        assertTrue(found.isPresent)
        assertEquals("build-deploy", found.get().pipelineName)
        assertEquals("RUNNING", found.get().status)
        assertEquals("agent-1", found.get().agentId)
        assertEquals(1700000000L, found.get().startedAt)
        assertEquals("manual", found.get().triggerInfo)
    }

    @Test
    fun `steps column persists and retrieves JSON correctly`() {
        val stepsJson = """[{"name":"build","status":"PASSED","detail":"ok","elapsed_ms":100}]"""
        pipelineRepository.save(
            PipelineRunEntity(
                id = "pipe-1",
                agentId = "agent-1",
                pipelineName = "build",
                status = "RUNNING",
                steps = stepsJson,
                startedAt = 100L,
            ),
        )

        val found = pipelineRepository.findById("pipe-1")

        assertTrue(found.isPresent)
        assertEquals(stepsJson, found.get().steps)
    }

    @Test
    fun `findByAgentId returns matching pipelines only`() {
        pipelineRepository.save(
            PipelineRunEntity(id = "p1", agentId = "agent-1", pipelineName = "build", status = "RUNNING", startedAt = 100L),
        )
        pipelineRepository.save(
            PipelineRunEntity(id = "p2", agentId = "agent-1", pipelineName = "test", status = "QUEUED", startedAt = 200L),
        )
        pipelineRepository.save(
            PipelineRunEntity(id = "p3", agentId = "agent-2", pipelineName = "deploy", status = "RUNNING", startedAt = 300L),
        )

        val result = pipelineRepository.findByAgentId("agent-1", PageRequest.of(0, 20))

        assertEquals(2, result.totalElements)
        assertTrue(result.content.all { it.agentId == "agent-1" })
    }

    @Test
    fun `findByStatus returns matching pipelines only`() {
        pipelineRepository.save(
            PipelineRunEntity(id = "p1", agentId = "agent-1", pipelineName = "build", status = "RUNNING", startedAt = 100L),
        )
        pipelineRepository.save(
            PipelineRunEntity(id = "p2", agentId = "agent-1", pipelineName = "test", status = "QUEUED", startedAt = 200L),
        )
        pipelineRepository.save(
            PipelineRunEntity(id = "p3", agentId = "agent-2", pipelineName = "deploy", status = "RUNNING", startedAt = 300L),
        )

        val result = pipelineRepository.findByStatus("RUNNING", PageRequest.of(0, 20))

        assertEquals(2, result.totalElements)
        assertTrue(result.content.all { it.status == "RUNNING" })
    }

    @Test
    fun `findByAgentIdAndStatus returns matching pipelines only`() {
        pipelineRepository.save(
            PipelineRunEntity(id = "p1", agentId = "agent-1", pipelineName = "build", status = "RUNNING", startedAt = 100L),
        )
        pipelineRepository.save(
            PipelineRunEntity(id = "p2", agentId = "agent-1", pipelineName = "test", status = "QUEUED", startedAt = 200L),
        )
        pipelineRepository.save(
            PipelineRunEntity(id = "p3", agentId = "agent-2", pipelineName = "deploy", status = "RUNNING", startedAt = 300L),
        )

        val result = pipelineRepository.findByAgentIdAndStatus("agent-1", "RUNNING", PageRequest.of(0, 20))

        assertEquals(1, result.totalElements)
        assertEquals("p1", result.content[0].id)
    }

    @Test
    fun `foreign key enforced - save fails with invalid agentId`() {
        val entity = PipelineRunEntity(id = "p1", agentId = "nonexistent", pipelineName = "build", status = "RUNNING", startedAt = 100L)
        pipelineRepository.save(entity)

        assertThrows<Exception> {
            pipelineRepository.flush()
        }
    }

    @Test
    fun `pagination works correctly`() {
        for (i in 1..5) {
            pipelineRepository.save(
                PipelineRunEntity(id = "p$i", agentId = "agent-1", pipelineName = "build-$i", status = "RUNNING", startedAt = i.toLong()),
            )
        }

        val page = pipelineRepository.findAll(PageRequest.of(0, 3))

        assertEquals(3, page.content.size)
        assertEquals(5, page.totalElements)
        assertEquals(2, page.totalPages)
    }
}
