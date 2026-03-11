package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.ui.TestAgentFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AgentDetailScreenTest {

    @Test
    fun `should display agent name in top bar`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(name = "detail-agent"),
                    onBack = {},
                )
            }
        }
        // Name appears in both top bar and overview panel
        onAllNodesWithText("detail-agent").assertCountEquals(2)
    }

    @Test
    fun `should display three tabs`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(),
                    onBack = {},
                )
            }
        }
        onNodeWithText("Overview").assertIsDisplayed()
        onNodeWithText("Pipelines").assertIsDisplayed()
        onNodeWithText("Events").assertIsDisplayed()
    }

    @Test
    fun `should show overview tab selected by default`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(),
                    onBack = {},
                )
            }
        }
        onNodeWithText("Overview").assertIsSelected()
    }

    @Test
    fun `should switch to pipelines tab on click`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(),
                    onBack = {},
                )
            }
        }
        onNodeWithText("Pipelines").performClick()
        onNodeWithText("Pipelines").assertIsSelected()
        onNodeWithText("Pipelines content coming soon").assertIsDisplayed()
    }

    @Test
    fun `should switch to events tab on click`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(),
                    onBack = {},
                )
            }
        }
        onNodeWithText("Events").performClick()
        onNodeWithText("Events").assertIsSelected()
        onNodeWithText("Events content coming soon").assertIsDisplayed()
    }

    @Test
    fun `should show overview content when overview tab is selected`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(
                        name = "overview-agent",
                        status = Agent.AgentStatus.RUNNING,
                    ),
                    onBack = {},
                )
            }
        }
        onNodeWithText("Healthy").assertIsDisplayed()
    }

    @Test
    fun `should invoke onBack when back button is clicked`() = runComposeUiTest {
        var backClicked = false
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(),
                    onBack = { backClicked = true },
                )
            }
        }
        onNodeWithText("Back").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun `should display agent type in top bar`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                AgentDetailScreen(
                    agent = TestAgentFactory.create(type = Agent.AgentType.REVIEWER),
                    onBack = {},
                )
            }
        }
        // Type appears in both top bar and overview panel
        onAllNodesWithText("reviewer").assertCountEquals(2)
    }
}
