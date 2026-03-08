package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PipelineRunTest {
    @Test
    fun `should calculate duration when finishedAt is provided`() {
        val run =
            PipelineRun(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "build-pipeline",
                status = PipelineRunStatus.PASSED,
                steps = emptyList(),
                startedAt = 1000L,
                finishedAt = 5000L,
                triggerInfo = "manual",
            )
        assertEquals(4000L, run.duration)
    }

    @Test
    fun `should return null duration when finishedAt is null`() {
        val run =
            PipelineRun(
                id = "run-2",
                agentId = "agent-1",
                pipelineName = "deploy-pipeline",
                status = PipelineRunStatus.RUNNING,
                steps = emptyList(),
                startedAt = 1000L,
                finishedAt = null,
                triggerInfo = "webhook",
            )
        assertNull(run.duration)
    }

    @Test
    fun `should return zero duration when startedAt equals finishedAt`() {
        val run =
            PipelineRun(
                id = "run-3",
                agentId = "agent-1",
                pipelineName = "test-pipeline",
                status = PipelineRunStatus.PASSED,
                steps = emptyList(),
                startedAt = 3000L,
                finishedAt = 3000L,
                triggerInfo = "auto",
            )
        assertEquals(0L, run.duration)
    }

    @Test
    fun `should hold pipeline steps correctly`() {
        val steps =
            listOf(
                PipelineStep(
                    name = "checkout",
                    status = StepStatus.PASSED,
                    detail = "Checked out main branch",
                    elapsedMs = 500L,
                ),
                PipelineStep(
                    name = "build",
                    status = StepStatus.RUNNING,
                    detail = "Compiling sources",
                    elapsedMs = 1200L,
                ),
                PipelineStep(
                    name = "test",
                    status = StepStatus.PENDING,
                    detail = "",
                    elapsedMs = 0L,
                ),
            )
        val run =
            PipelineRun(
                id = "run-4",
                agentId = "agent-2",
                pipelineName = "ci-pipeline",
                status = PipelineRunStatus.RUNNING,
                steps = steps,
                startedAt = 1000L,
                finishedAt = null,
                triggerInfo = "commit-push",
            )
        assertEquals(3, run.steps.size)
        assertEquals("checkout", run.steps[0].name)
        assertEquals(StepStatus.PASSED, run.steps[0].status)
        assertEquals(StepStatus.RUNNING, run.steps[1].status)
        assertEquals(StepStatus.PENDING, run.steps[2].status)
    }

    @Test
    fun `should support all PipelineRunStatus values`() {
        val statuses = PipelineRunStatus.entries
        assertEquals(5, statuses.size)
        assertEquals(
            listOf(
                PipelineRunStatus.QUEUED,
                PipelineRunStatus.RUNNING,
                PipelineRunStatus.PASSED,
                PipelineRunStatus.FAILED,
                PipelineRunStatus.CANCELLED,
            ),
            statuses,
        )
    }

    @Test
    fun `should support all StepStatus values`() {
        val statuses = StepStatus.entries
        assertEquals(5, statuses.size)
        assertEquals(
            listOf(
                StepStatus.PENDING,
                StepStatus.RUNNING,
                StepStatus.PASSED,
                StepStatus.FAILED,
                StepStatus.SKIPPED,
            ),
            statuses,
        )
    }

    @Test
    fun `should support equality for identical pipeline runs`() {
        val steps =
            listOf(
                PipelineStep(name = "build", status = StepStatus.PASSED, detail = "ok", elapsedMs = 100L),
            )
        val run1 =
            PipelineRun(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "pipeline",
                status = PipelineRunStatus.PASSED,
                steps = steps,
                startedAt = 1000L,
                finishedAt = 2000L,
                triggerInfo = "manual",
            )
        val run2 =
            PipelineRun(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "pipeline",
                status = PipelineRunStatus.PASSED,
                steps = steps,
                startedAt = 1000L,
                finishedAt = 2000L,
                triggerInfo = "manual",
            )
        assertEquals(run1, run2)
    }
}
