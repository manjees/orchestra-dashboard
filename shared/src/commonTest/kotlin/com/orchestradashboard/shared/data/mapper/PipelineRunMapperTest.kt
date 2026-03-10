package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.PipelineStepDto
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PipelineRunMapperTest {
    private val mapper = PipelineRunMapper()

    @Test
    fun `toDomain maps valid DTO with all fields correctly`() {
        val dto =
            PipelineRunDto(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "Build Pipeline",
                status = "RUNNING",
                steps =
                    listOf(
                        PipelineStepDto(
                            name = "Compile",
                            status = "PASSED",
                            detail = "Compiled successfully",
                            elapsedMs = 1200L,
                        ),
                    ),
                startedAt = 1700000000L,
                finishedAt = 1700001000L,
                triggerInfo = "manual",
            )

        val result = mapper.toDomain(dto)

        assertEquals("run-1", result.id)
        assertEquals("agent-1", result.agentId)
        assertEquals("Build Pipeline", result.pipelineName)
        assertEquals(PipelineRunStatus.RUNNING, result.status)
        assertEquals(1, result.steps.size)
        assertEquals("Compile", result.steps[0].name)
        assertEquals(StepStatus.PASSED, result.steps[0].status)
        assertEquals("Compiled successfully", result.steps[0].detail)
        assertEquals(1200L, result.steps[0].elapsedMs)
        assertEquals(1700000000L, result.startedAt)
        assertEquals(1700001000L, result.finishedAt)
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
                startedAt = 1700000000L,
                finishedAt = null,
            )

        val result = mapper.toDomain(dto)

        assertNull(result.finishedAt)
        assertNull(result.duration)
    }

    @Test
    fun `toDomain maps steps list with all step statuses`() {
        val dto =
            PipelineRunDto(
                id = "run-3",
                agentId = "agent-1",
                pipelineName = "Full Pipeline",
                status = "PASSED",
                steps =
                    listOf(
                        PipelineStepDto("Step 1", "PENDING", "", 0L),
                        PipelineStepDto("Step 2", "RUNNING", "", 500L),
                        PipelineStepDto("Step 3", "PASSED", "OK", 1000L),
                        PipelineStepDto("Step 4", "FAILED", "Error", 200L),
                        PipelineStepDto("Step 5", "SKIPPED", "", 0L),
                    ),
                startedAt = 1700000000L,
                finishedAt = 1700002000L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(5, result.steps.size)
        assertEquals(StepStatus.PENDING, result.steps[0].status)
        assertEquals(StepStatus.RUNNING, result.steps[1].status)
        assertEquals(StepStatus.PASSED, result.steps[2].status)
        assertEquals(StepStatus.FAILED, result.steps[3].status)
        assertEquals(StepStatus.SKIPPED, result.steps[4].status)
    }

    @Test
    fun `toDomain handles unknown pipeline status by defaulting to QUEUED`() {
        val dto =
            PipelineRunDto(
                id = "run-4",
                agentId = "agent-1",
                pipelineName = "Test",
                status = "UNKNOWN_STATUS",
                startedAt = 0L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(PipelineRunStatus.QUEUED, result.status)
    }

    @Test
    fun `toDomain handles unknown step status by defaulting to PENDING`() {
        val dto =
            PipelineRunDto(
                id = "run-5",
                agentId = "agent-1",
                pipelineName = "Test",
                status = "RUNNING",
                steps = listOf(PipelineStepDto("Bad Step", "INVALID", "detail", 100L)),
                startedAt = 0L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(StepStatus.PENDING, result.steps[0].status)
    }

    @Test
    fun `toDomain handles empty steps list`() {
        val dto =
            PipelineRunDto(
                id = "run-6",
                agentId = "agent-1",
                pipelineName = "Empty",
                status = "QUEUED",
                startedAt = 0L,
            )

        val result = mapper.toDomain(dto)

        assertTrue(result.steps.isEmpty())
    }

    @Test
    fun `toDomain handles case-insensitive status parsing`() {
        val dto =
            PipelineRunDto(
                id = "run-7",
                agentId = "agent-1",
                pipelineName = "Test",
                status = "running",
                steps = listOf(PipelineStepDto("Step", "passed", "ok", 100L)),
                startedAt = 0L,
            )

        val result = mapper.toDomain(dto)

        assertEquals(PipelineRunStatus.RUNNING, result.status)
        assertEquals(StepStatus.PASSED, result.steps[0].status)
    }

    @Test
    fun `toDomain list variant maps all elements`() {
        val dtos =
            listOf(
                PipelineRunDto("r1", "a1", "P1", "RUNNING", emptyList(), 100L),
                PipelineRunDto("r2", "a2", "P2", "PASSED", emptyList(), 200L, 300L),
            )

        val results = mapper.toDomain(dtos)

        assertEquals(2, results.size)
        assertEquals("r1", results[0].id)
        assertEquals("r2", results[1].id)
    }

    @Test
    fun `toDomain list variant maps empty list`() {
        val results = mapper.toDomain(emptyList())
        assertTrue(results.isEmpty())
    }
}
