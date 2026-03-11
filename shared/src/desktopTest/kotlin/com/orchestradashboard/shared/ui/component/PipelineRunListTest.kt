package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.ui.TestPipelineRunFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PipelineRunListTest {
    @Test
    fun `should display all pipeline runs`() =
        runComposeUiTest {
            val runs = TestPipelineRunFactory.createList()
            setContent {
                DashboardTheme {
                    PipelineRunList(pipelineRuns = runs)
                }
            }
            onNodeWithText("deploy-pipeline").assertIsDisplayed()
            onNodeWithText("test-pipeline").assertIsDisplayed()
            onNodeWithText("build-pipeline").assertIsDisplayed()
        }

    @Test
    fun `should show empty state when no pipeline runs`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    PipelineRunList(pipelineRuns = emptyList())
                }
            }
            onNodeWithText("No pipeline runs yet.").assertIsDisplayed()
        }
}
