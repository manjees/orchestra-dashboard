package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineStepDto
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MonitoredPipelineMapperTest {
    private val mapper = MonitoredPipelineMapper()

    private val sampleDto =
        OrchestratorPipelineDto(
            id = "pipe-1",
            projectName = "my-project",
            issueNum = 42,
            issueTitle = "Fix the bug",
            mode = "sequential",
            status = "running",
            currentStep = "testing",
            startedAt = "2024-06-15T10:30:00Z",
            steps =
                listOf(
                    PipelineStepDto("planning", "passed", 5.0),
                    PipelineStepDto("coding", "running", 12.5),
                    PipelineStepDto("testing", "pending", 0.0),
                ),
            elapsedTotalSec = 17.5,
        )

    @Test
    fun `maps OrchestratorPipelineDto to MonitoredPipeline correctly`() {
        val result = mapper.mapToDomain(sampleDto)
        assertEquals("pipe-1", result.id)
        assertEquals("my-project", result.projectName)
        assertEquals(42, result.issueNum)
        assertEquals("Fix the bug", result.issueTitle)
        assertEquals("sequential", result.mode)
        assertEquals(PipelineRunStatus.RUNNING, result.status)
        assertEquals(3, result.steps.size)
        assertEquals(17.5, result.elapsedTotalSec)
    }

    @Test
    fun `maps step status strings to StepStatus enum`() {
        assertEquals(StepStatus.PASSED, mapper.parseStepStatus("passed"))
        assertEquals(StepStatus.RUNNING, mapper.parseStepStatus("running"))
        assertEquals(StepStatus.FAILED, mapper.parseStepStatus("failed"))
        assertEquals(StepStatus.SKIPPED, mapper.parseStepStatus("skipped"))
    }

    @Test
    fun `handles unknown step status gracefully defaults to PENDING`() {
        assertEquals(StepStatus.PENDING, mapper.parseStepStatus("unknown_status"))
        assertEquals(StepStatus.PENDING, mapper.parseStepStatus(""))
    }

    @Test
    fun `converts elapsedSec to elapsedMs`() {
        val result = mapper.mapToDomain(sampleDto)
        assertEquals(5000L, result.steps[0].elapsedMs)
        assertEquals(12500L, result.steps[1].elapsedMs)
        assertEquals(0L, result.steps[2].elapsedMs)
    }

    @Test
    fun `maps startedAt ISO string to epoch milliseconds`() {
        val result = mapper.mapToDomain(sampleDto)
        assertEquals(1718447400000L, result.startedAtMs)
    }

    @Test
    fun `maps SUCCESS status to PASSED`() {
        assertEquals(StepStatus.PASSED, mapper.parseStepStatus("SUCCESS"))
        assertEquals(PipelineRunStatus.PASSED, mapper.parseRunStatus("SUCCESS"))
    }

    @Test
    fun `maps ERROR status to FAILED`() {
        assertEquals(StepStatus.FAILED, mapper.parseStepStatus("ERROR"))
        assertEquals(PipelineRunStatus.FAILED, mapper.parseRunStatus("ERROR"))
    }

    @Test
    fun `running step has non-null startedAtMs`() {
        val result = mapper.mapToDomain(sampleDto)
        val runningStep = result.steps.first { it.status == StepStatus.RUNNING }
        assertTrue(runningStep.startedAtMs != null)
    }

    @Test
    fun `passed step has null startedAtMs`() {
        val result = mapper.mapToDomain(sampleDto)
        val passedStep = result.steps.first { it.status == StepStatus.PASSED }
        assertNull(passedStep.startedAtMs)
    }
}
