package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.ui.TestAgentFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AgentCardTest {
    @Test
    fun `should display agent name`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentCard(
                        agent = TestAgentFactory.create(name = "my-worker"),
                        isSelected = false,
                        onClick = {},
                    )
                }
            }
            onNodeWithText("my-worker").assertIsDisplayed()
        }

    @Test
    fun `should display agent type as lowercase badge`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentCard(
                        agent = TestAgentFactory.create(type = Agent.AgentType.ORCHESTRATOR),
                        isSelected = false,
                        onClick = {},
                    )
                }
            }
            onNodeWithText("orchestrator").assertIsDisplayed()
        }

    @Test
    fun `should display Last seen label with relative time`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentCard(
                        agent = TestAgentFactory.create(),
                        isSelected = false,
                        onClick = {},
                    )
                }
            }
            onNodeWithText("Last seen", substring = true).assertIsDisplayed()
        }

    @Test
    fun `should invoke onClick when card is clicked`() =
        runComposeUiTest {
            var clicked = false
            setContent {
                DashboardTheme {
                    AgentCard(
                        agent = TestAgentFactory.create(name = "clickable-agent"),
                        isSelected = false,
                        onClick = { clicked = true },
                    )
                }
            }
            onNodeWithText("clickable-agent").performClick()
            assertTrue(clicked)
        }
}
