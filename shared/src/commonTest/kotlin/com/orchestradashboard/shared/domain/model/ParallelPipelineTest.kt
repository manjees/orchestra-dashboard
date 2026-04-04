package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParallelPipelineTest {
    // ─── ParallelPipelineGroup ────────────────────────────────────────────────

    @Test
    fun `ParallelPipelineGroup with empty pipelines has zero progress`() {
        val group =
            ParallelPipelineGroup(
                parentPipelineId = "p1",
                pipelines = emptyList(),
                dependencies = emptyList(),
            )
        assertEquals(0f, group.progressFraction)
    }

    @Test
    fun `ParallelPipelineGroup progressFraction averages across all lanes`() {
        val lane1 =
            makePipeline(
                id = "lane-1",
                steps =
                    listOf(
                        MonitoredStep("a", StepStatus.PASSED, 1000L, null, ""),
                        MonitoredStep("b", StepStatus.PASSED, 1000L, null, ""),
                    ),
            )
        val lane2 =
            makePipeline(
                id = "lane-2",
                steps =
                    listOf(
                        MonitoredStep("c", StepStatus.PASSED, 1000L, null, ""),
                        MonitoredStep("d", StepStatus.PENDING, 0L, null, ""),
                    ),
            )
        val group =
            ParallelPipelineGroup(
                parentPipelineId = "p1",
                pipelines = listOf(lane1, lane2),
                dependencies = emptyList(),
            )
        // lane1: 2/2 = 1.0, lane2: 1/2 = 0.5 → average = 0.75
        assertEquals(0.75f, group.progressFraction, absoluteTolerance = 0.001f)
    }

    @Test
    fun `ParallelPipelineGroup overallStatus is RUNNING if any lane is RUNNING`() {
        val group =
            ParallelPipelineGroup(
                parentPipelineId = "p1",
                pipelines =
                    listOf(
                        makePipeline(id = "lane-1", status = PipelineRunStatus.RUNNING),
                        makePipeline(id = "lane-2", status = PipelineRunStatus.PASSED),
                    ),
                dependencies = emptyList(),
            )
        assertEquals(PipelineRunStatus.RUNNING, group.overallStatus)
    }

    @Test
    fun `ParallelPipelineGroup overallStatus is FAILED if any lane is FAILED and none RUNNING`() {
        val group =
            ParallelPipelineGroup(
                parentPipelineId = "p1",
                pipelines =
                    listOf(
                        makePipeline(id = "lane-1", status = PipelineRunStatus.FAILED),
                        makePipeline(id = "lane-2", status = PipelineRunStatus.PASSED),
                    ),
                dependencies = emptyList(),
            )
        assertEquals(PipelineRunStatus.FAILED, group.overallStatus)
    }

    @Test
    fun `ParallelPipelineGroup overallStatus is PASSED when all lanes PASSED`() {
        val group =
            ParallelPipelineGroup(
                parentPipelineId = "p1",
                pipelines =
                    listOf(
                        makePipeline(id = "lane-1", status = PipelineRunStatus.PASSED),
                        makePipeline(id = "lane-2", status = PipelineRunStatus.PASSED),
                    ),
                dependencies = emptyList(),
            )
        assertEquals(PipelineRunStatus.PASSED, group.overallStatus)
    }

    @Test
    fun `ParallelPipelineGroup activeLaneCount returns count of RUNNING pipelines`() {
        val group =
            ParallelPipelineGroup(
                parentPipelineId = "p1",
                pipelines =
                    listOf(
                        makePipeline(id = "lane-1", status = PipelineRunStatus.RUNNING),
                        makePipeline(id = "lane-2", status = PipelineRunStatus.RUNNING),
                        makePipeline(id = "lane-3", status = PipelineRunStatus.PASSED),
                    ),
                dependencies = emptyList(),
            )
        assertEquals(2, group.activeLaneCount)
    }

    // ─── PipelineDependency ───────────────────────────────────────────────────

    @Test
    fun `PipelineDependency holds source and target lane IDs`() {
        val dep = PipelineDependency("lane-1", "lane-2", DependencyType.BLOCKS_START)
        assertEquals("lane-1", dep.sourceLaneId)
        assertEquals("lane-2", dep.targetLaneId)
    }

    @Test
    fun `PipelineDependency with BLOCKS_START type is correctly modeled`() {
        val dep = PipelineDependency("lane-1", "lane-2", DependencyType.BLOCKS_START)
        assertEquals(DependencyType.BLOCKS_START, dep.type)
    }

    @Test
    fun `PipelineDependency with PROVIDES_INPUT type is correctly modeled`() {
        val dep = PipelineDependency("lane-1", "lane-2", DependencyType.PROVIDES_INPUT)
        assertEquals(DependencyType.PROVIDES_INPUT, dep.type)
    }

    // ─── MonitoredPipeline.isParallel ─────────────────────────────────────────

    @Test
    fun `MonitoredPipeline isParallel returns true for mode parallel`() {
        val pipeline = makePipeline(id = "p1", mode = "parallel")
        assertTrue(pipeline.isParallel)
    }

    @Test
    fun `MonitoredPipeline isParallel returns false for mode sequential`() {
        val pipeline = makePipeline(id = "p1", mode = "sequential")
        assertFalse(pipeline.isParallel)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun makePipeline(
        id: String,
        status: PipelineRunStatus = PipelineRunStatus.QUEUED,
        mode: String = "parallel",
        steps: List<MonitoredStep> = emptyList(),
    ) = MonitoredPipeline(
        id = id,
        projectName = "test-project",
        issueNum = 1,
        issueTitle = "Test Issue",
        mode = mode,
        status = status,
        steps = steps,
        startedAtMs = 1718447400000L,
        elapsedTotalSec = 0.0,
    )
}
