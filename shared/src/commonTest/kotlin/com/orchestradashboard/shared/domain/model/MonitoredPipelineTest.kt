package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MonitoredPipelineTest {
    private fun step(
        name: String,
        status: StepStatus = StepStatus.PENDING,
        elapsedMs: Long = 0L,
        startedAtMs: Long? = null,
    ) = MonitoredStep(name, status, elapsedMs, startedAtMs, "")

    private fun pipeline(
        steps: List<MonitoredStep> = emptyList(),
        mode: String = "sequential",
    ) = MonitoredPipeline(
        id = "p1",
        projectName = "proj",
        issueNum = 1,
        issueTitle = "title",
        mode = mode,
        status = PipelineRunStatus.RUNNING,
        steps = steps,
        startedAtMs = 0L,
        elapsedTotalSec = 0.0,
    )

    @Test
    fun `currentRunningStep returns first step with RUNNING status`() {
        val steps =
            listOf(
                step("a", StepStatus.PASSED),
                step("b", StepStatus.RUNNING),
                step("c", StepStatus.PENDING),
            )
        assertEquals("b", pipeline(steps).currentRunningStep?.name)
    }

    @Test
    fun `currentRunningStep returns null when no step is RUNNING`() {
        val steps = listOf(step("a", StepStatus.PASSED), step("b", StepStatus.PENDING))
        assertNull(pipeline(steps).currentRunningStep)
    }

    @Test
    fun `progressFraction returns completed count over total steps`() {
        val steps =
            listOf(
                step("a", StepStatus.PASSED),
                step("b", StepStatus.FAILED),
                step("c", StepStatus.RUNNING),
                step("d", StepStatus.PENDING),
            )
        assertEquals(0.5f, pipeline(steps).progressFraction)
    }

    @Test
    fun `progressFraction returns 0 for empty steps`() {
        assertEquals(0f, pipeline(emptyList()).progressFraction)
    }

    @Test
    fun `progressFraction counts SKIPPED as completed`() {
        val steps =
            listOf(
                step("a", StepStatus.PASSED),
                step("b", StepStatus.SKIPPED),
            )
        assertEquals(1f, pipeline(steps).progressFraction)
    }

    @Test
    fun `isParallel returns true when mode is parallel`() {
        assertTrue(pipeline(mode = "parallel").isParallel)
    }

    @Test
    fun `isParallel returns false when mode is sequential`() {
        assertFalse(pipeline(mode = "sequential").isParallel)
    }
}
