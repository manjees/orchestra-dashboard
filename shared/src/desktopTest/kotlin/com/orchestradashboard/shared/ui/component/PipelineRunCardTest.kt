package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.ui.TestPipelineFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PipelineRunCardTest {
    @Test
    fun `displays pipeline name and status`() =
        runComposeUiTest {
            val run = TestPipelineFactory.createRun()
            setContent { DashboardTheme { PipelineRunCard(run = run, isExpanded = false, onToggleExpand = {}) } }
            onNodeWithText("Build Pipeline").assertIsDisplayed()
        }

    @Test
    fun `displays formatted duration for completed run`() =
        runComposeUiTest {
            val run = TestPipelineFactory.createRun(startedAt = 0L, finishedAt = 15_000L)
            setContent { DashboardTheme { PipelineRunCard(run = run, isExpanded = false, onToggleExpand = {}) } }
            onNodeWithText("15s").assertIsDisplayed()
        }

    @Test
    fun `displays running indicator for in-progress run`() =
        runComposeUiTest {
            val run = TestPipelineFactory.createRunningRun()
            setContent { DashboardTheme { PipelineRunCard(run = run, isExpanded = false, onToggleExpand = {}) } }
            onNodeWithText("running\u2026").assertIsDisplayed()
        }

    @Test
    fun `expanded card shows all step names`() =
        runComposeUiTest {
            val run = TestPipelineFactory.createRun()
            setContent { DashboardTheme { PipelineRunCard(run = run, isExpanded = true, onToggleExpand = {}) } }
            onNodeWithText("Compile").assertIsDisplayed()
            onNodeWithText("Test").assertIsDisplayed()
        }

    @Test
    fun `click calls onToggleExpand`() =
        runComposeUiTest {
            var clicked = false
            val run = TestPipelineFactory.createRun()
            setContent {
                DashboardTheme {
                    PipelineRunCard(run = run, isExpanded = false, onToggleExpand = { clicked = true })
                }
            }
            onNodeWithText("Build Pipeline").performClick()
            waitForIdle()
            assert(clicked)
        }
}
