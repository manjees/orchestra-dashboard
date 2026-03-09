package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.CreatePipelineRunRequest
import com.orchestradashboard.server.model.PipelineRunEntity
import com.orchestradashboard.server.model.PipelineRunMapper
import com.orchestradashboard.server.model.PipelineStepResponse
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.PipelineRunJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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

    private val defaultEntity =
        PipelineRunEntity(
            id = "run-1",
            agentId = "agent-1",
            pipelineName = "CI Pipeline",
            status = "RUNNING",
            steps = "[]",
            startedAt = 1700000000L,
        )

    @Test
    fun `getAllPipelineRuns returns mapped responses`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findAll(pageable)).thenReturn(PageImpl(listOf(defaultEntity)))

        val result = service.getAllPipelineRuns(null, null, pageable)

        assertEquals(1, result.size)
        assertEquals("run-1", result[0].id)
    }

    @Test
    fun `getAllPipelineRuns returns empty list when none exist`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findAll(pageable)).thenReturn(PageImpl(emptyList()))

        val result = service.getAllPipelineRuns(null, null, pageable)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getPipelineRun returns response when found`() {
        whenever(pipelineRepository.findById("run-1")).thenReturn(Optional.of(defaultEntity))

        val result = service.getPipelineRun("run-1")

        assertEquals("run-1", result.id)
        assertEquals("CI Pipeline", result.pipelineName)
    }

    @Test
    fun `getPipelineRun throws NoSuchElementException when not found`() {
        whenever(pipelineRepository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.getPipelineRun("missing")
        }
    }

    @Test
    fun `getPipelineRunsByAgentId delegates to repository with pagination`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findByAgentId("agent-1", pageable)).thenReturn(PageImpl(listOf(defaultEntity)))

        val result = service.getAllPipelineRuns("agent-1", null, pageable)

        assertEquals(1, result.size)
        verify(pipelineRepository).findByAgentId("agent-1", pageable)
    }

    @Test
    fun `getPipelineRunsByStatus delegates to repository with pagination`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findByStatus("RUNNING", pageable)).thenReturn(PageImpl(listOf(defaultEntity)))

        val result = service.getAllPipelineRuns(null, "RUNNING", pageable)

        assertEquals(1, result.size)
        verify(pipelineRepository).findByStatus("RUNNING", pageable)
    }

    @Test
    fun `getPipelineRunsByAgentIdAndStatus delegates to repository with pagination`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineRepository.findByAgentIdAndStatus("agent-1", "RUNNING", pageable)).thenReturn(PageImpl(listOf(defaultEntity)))

        val result = service.getAllPipelineRuns("agent-1", "RUNNING", pageable)

        assertEquals(1, result.size)
        verify(pipelineRepository).findByAgentIdAndStatus("agent-1", "RUNNING", pageable)
    }

    @Test
    fun `createPipelineRun saves entity and returns response`() {
        val request =
            CreatePipelineRunRequest(
                agentId = "agent-1",
                pipelineName = "CI Pipeline",
                steps = listOf(PipelineStepResponse(name = "Build", status = "PENDING", detail = "", elapsedMs = 0L)),
                triggerInfo = "manual",
            )
        whenever(agentRepository.existsById("agent-1")).thenReturn(true)
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        val result = service.createPipelineRun(request)

        assertEquals("agent-1", result.agentId)
        assertEquals("CI Pipeline", result.pipelineName)
        assertEquals("QUEUED", result.status)
        assertEquals(1, result.steps.size)
        assertEquals("manual", result.triggerInfo)
    }

    @Test
    fun `createPipelineRun auto-assigns UUID`() {
        val request = CreatePipelineRunRequest(agentId = "agent-1", pipelineName = "Pipeline")
        whenever(agentRepository.existsById("agent-1")).thenReturn(true)
        val captor = argumentCaptor<PipelineRunEntity>()
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        service.createPipelineRun(request)

        verify(pipelineRepository).save(captor.capture())
        assertNotNull(captor.firstValue.id)
        assertTrue(captor.firstValue.id.isNotBlank())
    }

    @Test
    fun `createPipelineRun validates agent exists`() {
        val request = CreatePipelineRunRequest(agentId = "missing", pipelineName = "Pipeline")
        whenever(agentRepository.existsById("missing")).thenReturn(false)

        assertThrows<NoSuchElementException> {
            service.createPipelineRun(request)
        }
    }

    @Test
    fun `updateStatus updates status field`() {
        whenever(pipelineRepository.findById("run-1")).thenReturn(Optional.of(defaultEntity))
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        val result = service.updateStatus("run-1", "RUNNING")

        assertEquals("RUNNING", result.status)
    }

    @Test
    fun `updateStatus sets finishedAt for terminal statuses`() {
        whenever(pipelineRepository.findById("run-1")).thenReturn(Optional.of(defaultEntity))
        val captor = argumentCaptor<PipelineRunEntity>()
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        service.updateStatus("run-1", "PASSED")

        verify(pipelineRepository).save(captor.capture())
        assertNotNull(captor.firstValue.finishedAt)
    }

    @Test
    fun `updateStatus preserves null finishedAt for non-terminal statuses`() {
        whenever(pipelineRepository.findById("run-1")).thenReturn(Optional.of(defaultEntity))
        val captor = argumentCaptor<PipelineRunEntity>()
        whenever(pipelineRepository.save(any<PipelineRunEntity>())).thenAnswer { it.arguments[0] as PipelineRunEntity }

        service.updateStatus("run-1", "RUNNING")

        verify(pipelineRepository).save(captor.capture())
        assertNull(captor.firstValue.finishedAt)
    }

    @Test
    fun `updateStatus throws for non-existent run`() {
        whenever(pipelineRepository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.updateStatus("missing", "PASSED")
        }
    }
}
