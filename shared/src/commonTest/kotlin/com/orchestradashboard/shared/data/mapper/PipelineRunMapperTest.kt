package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.PipelineStepDto
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.PipelineStep
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PipelineRunMapperTest {
    private val mapper = PipelineRunMapper()

    @Test
    fun `toDomain maps complete PipelineRunDto correctly`() {
        val dto =
            PipelineRunDto(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "Build Pipeline",
                status = "RUNNING",
                steps =
                    listOf(
                        PipelineStepDto(name = "Compile", status = "PASSED", detail = "OK", elapsedMs = 1200L),
                        PipelineStepDto(name = "Test", status = "RUNNING", detail = "In progress", elapsedMs = 500L),
                    ),
                startedAt = 1000L,
                finishedAt = 2000L,
                triggerInfo = "manual",
            )

        val result = mapper.toDomain(dto)

        assertEquals("run-1", result.id)
        assertEquals("agent-1", result.agentId)
        assertEquals("Build Pipeline", result.pipelineName)
        assertEquals(PipelineRunStatus.RUNNING, result.status)
        assertEquals(2, result.steps.size)
        assertEquals("Compile", result.steps[0].name)
        assertEquals(StepStatus.PASSED, result.steps[0].status)
        assertEquals("OK", result.steps[0].detail)
        assertEquals(1200L, result.steps[0].elapsedMs)
        assertEquals("Test", result.steps[1].name)
        assertEquals(StepStatus.RUNNING, result.steps[1].status)
        assertEquals(1000L, result.startedAt)
        assertEquals(2000L, result.finishedAt)
        assertEquals("manual", result.triggerInfo)
    }

    @Test
    fun `toDomain maps null finishedAt correctly`() {
        val dto =
            PipelineRunDto(
                id = "run-2",
                agentId = "agent-1",
                pipelineName = "Deploy",
                status = "RUNNING",
                startedAt = 1000L,
                finishedAt = null,
            )

        val result = mapper.toDomain(dto)

        assertNull(result.finishedAt)
        assertNull(result.duration)
    }

    @Test
    fun `toDomain maps empty step list`() {
        val dto =
            PipelineRunDto(
                id = "run-3",
                agentId = "agent-1",
                pipelineName = "Empty",
                status = "QUEUED",
                startedAt = 1000L,
            )

        val result = mapper.toDomain(dto)

        assertTrue(result.steps.isEmpty())
    }

    @Test
    fun `toDomain handles unknown PipelineRunStatus by defaulting to QUEUED`() {
        val dto =
            PipelineRunDto(
                id = "run-4",
                agentId = "agent-1",
                pipelineName = "Unknown",
                status = "BOGUS",
                startedAt = 1000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(PipelineRunStatus.QUEUED, result.status)
    }

    @Test
    fun `toDomain handles unknown StepStatus by defaulting to PENDING`() {
        val dto =
            PipelineRunDto(
                id = "run-5",
                agentId = "agent-1",
                pipelineName = "BadStep",
                status = "RUNNING",
                steps = listOf(PipelineStepDto(name = "Bad", status = "INVALID")),
                startedAt = 1000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(StepStatus.PENDING, result.steps[0].status)
    }

    @Test
    fun `toDomain handles case-insensitive status parsing`() {
        val dto =
            PipelineRunDto(
                id = "run-6",
                agentId = "agent-1",
                pipelineName = "CaseTest",
                status = "running",
                steps = listOf(PipelineStepDto(name = "Step", status = "passed")),
                startedAt = 1000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(PipelineRunStatus.RUNNING, result.status)
        assertEquals(StepStatus.PASSED, result.steps[0].status)
    }

    @Test
    fun `toDomain list variant maps all elements`() {
        val dtos =
            listOf(
                PipelineRunDto("r1", "a1", "P1", "QUEUED", emptyList(), 100L),
                PipelineRunDto("r2", "a2", "P2", "RUNNING", emptyList(), 200L),
                PipelineRunDto("r3", "a3", "P3", "PASSED", emptyList(), 300L),
            )

        val results = mapper.toDomain(dtos)

        assertEquals(3, results.size)
        assertEquals("r1", results[0].id)
        assertEquals("r2", results[1].id)
        assertEquals("r3", results[2].id)
    }

    @Test
    fun `toDomain list variant maps empty list`() {
        val results = mapper.toDomain(emptyList())
        assertTrue(results.isEmpty())
    }

    @Test
    fun `toDto maps domain PipelineRun to DTO correctly`() {
        val domain =
            PipelineRun(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "Build",
                status = PipelineRunStatus.PASSED,
                steps =
                    listOf(
                        PipelineStep(name = "Compile", status = StepStatus.PASSED, detail = "OK", elapsedMs = 1200L),
                    ),
                startedAt = 1000L,
                finishedAt = 2000L,
                triggerInfo = "ci",
            )

        val result = mapper.toDto(domain)

        assertEquals("run-1", result.id)
        assertEquals("agent-1", result.agentId)
        assertEquals("Build", result.pipelineName)
        assertEquals("PASSED", result.status)
        assertEquals(1, result.steps.size)
        assertEquals("Compile", result.steps[0].name)
        assertEquals("PASSED", result.steps[0].status)
        assertEquals("OK", result.steps[0].detail)
        assertEquals(1200L, result.steps[0].elapsedMs)
        assertEquals(1000L, result.startedAt)
        assertEquals(2000L, result.finishedAt)
        assertEquals("ci", result.triggerInfo)
    }

    @Test
    fun `toDto maps null finishedAt`() {
        val domain =
            PipelineRun(
                id = "run-2",
                agentId = "agent-1",
                pipelineName = "Deploy",
                status = PipelineRunStatus.RUNNING,
                steps = emptyList(),
                startedAt = 1000L,
                finishedAt = null,
                triggerInfo = "",
            )

        val result = mapper.toDto(domain)

        assertNull(result.finishedAt)
    }

    @Test
    fun `toDto round-trips correctly`() {
        val originalDto =
            PipelineRunDto(
                id = "rt",
                agentId = "a1",
                pipelineName = "RoundTrip",
                status = "RUNNING",
                steps =
                    listOf(
                        PipelineStepDto(name = "Step1", status = "PASSED", detail = "done", elapsedMs = 100L),
                    ),
                startedAt = 1000L,
                finishedAt = 2000L,
                triggerInfo = "test",
            )

        val roundTripped = mapper.toDto(mapper.toDomain(originalDto))

        assertEquals(originalDto, roundTripped)
    }
}
