package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.ui.TestPipelineRunFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PipelineRunCardTest {
    @Test
    fun `should display pipeline name`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    PipelineRunCard(
                        pipelineRun = TestPipelineRunFactory.create(pipelineName = "deploy-pipeline"),
                    )
                }
            }
            onNodeWithText("deploy-pipeline").assertIsDisplayed()
        }

    @Test
    fun `should display pipeline status`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    PipelineRunCard(
                        pipelineRun = TestPipelineRunFactory.create(status = PipelineRunStatus.PASSED),
                    )
                }
            }
            onNodeWithText("PASSED").assertIsDisplayed()
        }

    @Test
    fun `should display status icon`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    PipelineRunCard(
                        pipelineRun = TestPipelineRunFactory.create(status = PipelineRunStatus.FAILED),
                    )
                }
            }
            onNodeWithContentDescription("FAILED").assertIsDisplayed()
        }

    @Test
    fun `should display duration for completed runs`() =
        runComposeUiTest {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            setContent {
                DashboardTheme {
                    PipelineRunCard(
                        pipelineRun =
                            TestPipelineRunFactory.create(
                                startedAt = now - 65_000L,
                                finishedAt = now,
                            ),
                    )
                }
            }
            onNodeWithText("1m 5s").assertIsDisplayed()
        }

    @Test
    fun `should not show steps when collapsed`() =
        runComposeUiTest {
            val steps =
                listOf(
                    TestPipelineRunFactory.createStep(name = "Build", status = StepStatus.PASSED),
                )
            setContent {
                DashboardTheme {
                    PipelineRunCard(
                        pipelineRun = TestPipelineRunFactory.create(steps = steps),
                    )
                }
            }
            onAllNodesWithText("Build").assertCountEquals(0)
        }

    @Test
    fun `should show steps when expanded`() =
        runComposeUiTest {
            val steps =
                listOf(
                    TestPipelineRunFactory.createStep(name = "Build", status = StepStatus.PASSED),
                    TestPipelineRunFactory.createStep(name = "Test", status = StepStatus.FAILED),
                )
            setContent {
                DashboardTheme {
                    PipelineRunCard(
                        pipelineRun =
                            TestPipelineRunFactory.create(
                                pipelineName = "my-pipeline",
                                steps = steps,
                            ),
                    )
                }
            }
            onNodeWithText("my-pipeline").performClick()
            waitForIdle()
            onNodeWithText("Build").assertIsDisplayed()
            onNodeWithText("Test").assertIsDisplayed()
        }
}
