package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.CreatePipelineRunRequest
import com.orchestradashboard.server.model.PipelineRunEntity
import com.orchestradashboard.server.model.PipelineRunMapper
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.PipelineRunJpaRepository
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

class PipelineServiceTest {
    private val pipelineRepository: PipelineRunJpaRepository = mock()
    private val agentRepository: AgentJpaRepository = mock()
    private val mapper = PipelineRunMapper()
    private val service = PipelineService(pipelineRepository, agentRepository, mapper)

    private val sampleEntity =
        PipelineRunEntity(
            id = "pipe-1",
            agentId = "agent-1",
            pipelineName = "build-deploy",
            status = "RUNNING",
            steps = "[]",
            startedAt = 1700000000L,
            triggerInfo = "manual",
        )

    @Test
    fun `getAllPipelines returns paginated results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findAll(pageable)).thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getPipelines(null, null, pageable)

        assertEquals(1, result.totalElements)
        assertEquals("pipe-1", result.content[0].id)
        verify(pipelineRepository).findAll(pageable)
    }

    @Test
    fun `getPipelinesByAgentId returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findByAgentId("agent-1", pageable)).thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getPipelines("agent-1", null, pageable)

        assertEquals(1, result.totalElements)
        verify(pipelineRepository).findByAgentId("agent-1", pageable)
    }

    @Test
    fun `getPipelinesByStatus returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findByStatus("RUNNING", pageable)).thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getPipelines(null, "RUNNING", pageable)

        assertEquals(1, result.totalElements)
        verify(pipelineRepository).findByStatus("RUNNING", pageable)
    }

    @Test
    fun `getPipelinesByAgentIdAndStatus returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(
            pipelineRepository.findByAgentIdAndStatus("agent-1", "RUNNING", pageable),
        ).thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getPipelines("agent-1", "RUNNING", pageable)

        assertEquals(1, result.totalElements)
        verify(pipelineRepository).findByAgentIdAndStatus("agent-1", "RUNNING", pageable)
    }

    @Test
    fun `getPipeline returns pipeline when found`() {
        whenever(pipelineRepository.findById("pipe-1")).thenReturn(Optional.of(sampleEntity))

        val result = service.getPipeline("pipe-1")

        assertEquals("pipe-1", result.id)
        assertEquals("build-deploy", result.pipelineName)
    }

    @Test
    fun `getPipeline throws when not found`() {
        whenever(pipelineRepository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.getPipeline("missing")
        }
    }

    @Test
    fun `createPipeline creates with valid agentId`() {
        val agent = AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L)
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent))
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        val request = CreatePipelineRunRequest(id = "pipe-1", agentId = "agent-1", pipelineName = "build-deploy")
        val result = service.createPipeline(request)

        assertEquals("pipe-1", result.id)
        assertEquals("QUEUED", result.status)
        assertTrue(result.startedAt > 0)
    }

    @Test
    fun `createPipeline throws when agent not found`() {
        whenever(agentRepository.findById("missing")).thenReturn(Optional.empty())

        val request = CreatePipelineRunRequest(agentId = "missing", pipelineName = "build")

        val ex =
            assertThrows<NoSuchElementException> {
                service.createPipeline(request)
            }
        assertTrue(ex.message!!.contains("Agent"))
    }

    @Test
    fun `createPipeline auto-assigns UUID when id not provided`() {
        val agent = AgentEntity(id = "agent-1", name = "Alpha", type = "WORKER", status = "RUNNING", lastHeartbeat = 100L)
        whenever(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent))
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        val request = CreatePipelineRunRequest(agentId = "agent-1", pipelineName = "build")
        val captor = argumentCaptor<PipelineRunEntity>()

        service.createPipeline(request)

        verify(pipelineRepository).save(captor.capture())
        assertNotNull(captor.firstValue.id)
        assertTrue(captor.firstValue.id.isNotBlank())
    }

    @Test
    fun `updateStatus updates pipeline status`() {
        whenever(pipelineRepository.findById("pipe-1")).thenReturn(Optional.of(sampleEntity))
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        val result = service.updateStatus("pipe-1", "RUNNING")

        assertEquals("RUNNING", result.status)
    }

    @Test
    fun `updateStatus sets finishedAt for terminal statuses`() {
        val entity = sampleEntity.copy(status = "RUNNING", finishedAt = null)
        whenever(pipelineRepository.findById("pipe-1")).thenReturn(Optional.of(entity))
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        val result = service.updateStatus("pipe-1", "PASSED")

        assertEquals("PASSED", result.status)
        assertNotNull(result.finishedAt)
    }

    @Test
    fun `updateStatus throws when pipeline not found`() {
        whenever(pipelineRepository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.updateStatus("missing", "RUNNING")
        }
    }
}
