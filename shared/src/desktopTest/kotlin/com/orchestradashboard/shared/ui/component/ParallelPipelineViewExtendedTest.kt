package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.DependencyType
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.MonitoredStep
import com.orchestradashboard.shared.domain.model.PipelineDependency
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ParallelPipelineViewExtendedTest {

    // ─── Empty state ──────────────────────────────────────────────────────────

    @Test
    fun `parallelView emptyPipelines showsEmptyMessage`() =
        runComposeUiTest {
            setContent { DashboardTheme { ParallelPipelineView(pipelines = emptyList()) } }
            onNodeWithTag("parallel_empty_state").assertIsDisplayed()
            onNodeWithText("No parallel lanes").assertIsDisplayed()
        }

    // ─── Lane status display ──────────────────────────────────────────────────

    @Test
    fun `parallelView laneHeaderShowsRunningStatus`() =
        runComposeUiTest {
            val pipeline = makePipeline("lane-1", status = PipelineRunStatus.RUNNING)
            setContent { DashboardTheme { ParallelPipelineView(pipelines = listOf(pipeline)) } }
            onNodeWithText("RUNNING").assertIsDisplayed()
        }

    @Test
    fun `parallelView laneHeaderShowsPassedStatus`() =
        runComposeUiTest {
            val pipeline = makePipeline("lane-1", status = PipelineRunStatus.PASSED)
            setContent { DashboardTheme { ParallelPipelineView(pipelines = listOf(pipeline)) } }
            onNodeWithText("PASSED").assertIsDisplayed()
        }

    @Test
    fun `parallelView laneHeaderShowsFailedStatus`() =
        runComposeUiTest {
            val pipeline = makePipeline("lane-1", status = PipelineRunStatus.FAILED)
            setContent { DashboardTheme { ParallelPipelineView(pipelines = listOf(pipeline)) } }
            onNodeWithText("FAILED").assertIsDisplayed()
        }

    @Test
    fun `parallelView laneHeaderShowsCancelledStatus`() =
        runComposeUiTest {
            val pipeline = makePipeline("lane-1", status = PipelineRunStatus.CANCELLED)
            setContent { DashboardTheme { ParallelPipelineView(pipelines = listOf(pipeline)) } }
            onNodeWithText("CANCELLED").assertIsDisplayed()
        }

    @Test
    fun `parallelView laneHeaderShowsQueuedStatus`() =
        runComposeUiTest {
            val pipeline = makePipeline("lane-1", status = PipelineRunStatus.QUEUED)
            setContent { DashboardTheme { ParallelPipelineView(pipelines = listOf(pipeline)) } }
            onNodeWithText("QUEUED").assertIsDisplayed()
        }

    // ─── Issue info display ───────────────────────────────────────────────────

    @Test
    fun `parallelView laneHeaderShowsIssueInfo`() =
        runComposeUiTest {
            val pipeline =
                makePipeline("lane-1", issueNum = 42, issueTitle = "Fix login")
            setContent { DashboardTheme { ParallelPipelineView(pipelines = listOf(pipeline)) } }
            onNodeWithTag("lane_issue_lane-1").assertIsDisplayed()
            onNodeWithText("#42 Fix login").assertIsDisplayed()
        }

    // ─── Dependency legend ────────────────────────────────────────────────────

    @Test
    fun `parallelView withDependencies showsLegend`() =
        runComposeUiTest {
            val deps = listOf(PipelineDependency("lane-1", "lane-2", DependencyType.BLOCKS_START))
            setContent {
                DashboardTheme {
                    ParallelPipelineView(
                        pipelines = listOf(makePipeline("lane-1"), makePipeline("lane-2")),
                        dependencies = deps,
                    )
                }
            }
            onNodeWithTag("dependency_legend").assertIsDisplayed()
            onNodeWithText("Blocks").assertIsDisplayed()
            onNodeWithText("Provides Input").assertIsDisplayed()
        }

    @Test
    fun `parallelView noDependencies hidesLegend`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ParallelPipelineView(
                        pipelines = listOf(makePipeline("lane-1")),
                        dependencies = emptyList(),
                    )
                }
            }
            onNodeWithTag("dependency_legend").assertDoesNotExist()
        }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun makePipeline(
        id: String,
        status: PipelineRunStatus = PipelineRunStatus.RUNNING,
        steps: List<MonitoredStep> = listOf(makeStep("step1")),
        issueNum: Int = 1,
        issueTitle: String = "Test",
    ) = MonitoredPipeline(
        id = id,
        projectName = "test",
        issueNum = issueNum,
        issueTitle = issueTitle,
        mode = "parallel",
        status = status,
        steps = steps,
        startedAtMs = null,
        elapsedTotalSec = 0.0,
    )

    private fun makeStep(
        name: String,
        status: StepStatus = StepStatus.PENDING,
    ) = MonitoredStep(name = name, status = status, elapsedMs = 0L, startedAtMs = null, detail = "")
}
