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
class ParallelPipelineViewTest {
    @Test
    fun `parallelView displaysAllLanes`() =
        runComposeUiTest {
            val pipelines =
                listOf(
                    makePipeline("lane-1"),
                    makePipeline("lane-2"),
                    makePipeline("lane-3"),
                )
            setContent { DashboardTheme { ParallelPipelineView(pipelines = pipelines) } }
            onNodeWithTag("lane_header_lane-1").assertIsDisplayed()
            onNodeWithTag("lane_header_lane-2").assertIsDisplayed()
            onNodeWithTag("lane_header_lane-3").assertIsDisplayed()
        }

    @Test
    fun `parallelView emptyPipelines showsNothing`() =
        runComposeUiTest {
            setContent { DashboardTheme { ParallelPipelineView(pipelines = emptyList()) } }
            // No lane headers rendered — nodes won't exist
            onNodeWithTag("lane_header_lane-1").assertDoesNotExist()
        }

    @Test
    fun `parallelView singleLane noDependencies noArrowCanvas`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ParallelPipelineView(
                        pipelines = listOf(makePipeline("lane-1")),
                        dependencies = emptyList(),
                    )
                }
            }
            // Arrow overlay is not rendered when dependencies list is empty
            onNodeWithTag("dependency_arrows").assertDoesNotExist()
        }

    @Test
    fun `parallelView withDependencies showsArrowCanvas`() =
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
            onNodeWithTag("dependency_arrows").assertIsDisplayed()
        }

    @Test
    fun `parallelView laneShowsStepTimeline`() =
        runComposeUiTest {
            val pipeline =
                makePipeline(
                    "lane-1",
                    steps =
                        listOf(
                            makeStep("coding"),
                            makeStep("testing"),
                            makeStep("deploy"),
                        ),
                )
            setContent { DashboardTheme { ParallelPipelineView(pipelines = listOf(pipeline)) } }
            onNodeWithText("coding").assertIsDisplayed()
            onNodeWithText("testing").assertIsDisplayed()
            onNodeWithText("deploy").assertIsDisplayed()
        }

    @Test
    fun `parallelView laneHeaderShowsPipelineId`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ParallelPipelineView(pipelines = listOf(makePipeline("lane-1")))
                }
            }
            onNodeWithText("lane-1").assertIsDisplayed()
        }

    @Test
    fun `parallelView laneHeaderTagExists`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ParallelPipelineView(pipelines = listOf(makePipeline("my-lane")))
                }
            }
            onNodeWithTag("lane_header_my-lane").assertIsDisplayed()
        }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun makePipeline(
        id: String,
        steps: List<MonitoredStep> = listOf(makeStep("step1")),
    ) = MonitoredPipeline(
        id = id,
        projectName = "test",
        issueNum = 1,
        issueTitle = "Test",
        mode = "parallel",
        status = PipelineRunStatus.RUNNING,
        steps = steps,
        startedAtMs = null,
        elapsedTotalSec = 0.0,
    )

    private fun makeStep(name: String) =
        MonitoredStep(name = name, status = StepStatus.PENDING, elapsedMs = 0L, startedAtMs = null, detail = "")
}
