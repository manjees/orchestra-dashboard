package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.ui.TestAgentFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AgentOverviewPanelTest {
    @Test
    fun `should display agent name`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(agent = TestAgentFactory.create(name = "my-agent"))
                }
            }
            onNodeWithText("my-agent").assertIsDisplayed()
        }

    @Test
    fun `should display agent type`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent = TestAgentFactory.create(type = Agent.AgentType.ORCHESTRATOR),
                    )
                }
            }
            onNodeWithText("orchestrator").assertIsDisplayed()
        }

    @Test
    fun `should display agent status`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent = TestAgentFactory.create(status = Agent.AgentStatus.RUNNING),
                    )
                }
            }
            onNodeWithText("RUNNING").assertIsDisplayed()
        }

    @Test
    fun `should display Last seen label with relative time`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent =
                            TestAgentFactory.create(
                                lastHeartbeat = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - 5000L,
                            ),
                    )
                }
            }
            onNodeWithText("Last seen").assertIsDisplayed()
            onNodeWithText("5s ago").assertIsDisplayed()
        }

    @Test
    fun `should display healthy badge for running agent`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent = TestAgentFactory.create(status = Agent.AgentStatus.RUNNING),
                    )
                }
            }
            onNodeWithText("Healthy").assertIsDisplayed()
        }

    @Test
    fun `should display unhealthy badge for error agent`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent = TestAgentFactory.create(status = Agent.AgentStatus.ERROR),
                    )
                }
            }
            onNodeWithText("Unhealthy").assertIsDisplayed()
        }

    @Test
    fun `should display metadata entries`() =
        runComposeUiTest {
            val agent =
                TestAgentFactory.create().copy(
                    metadata = mapOf("version" to "1.2.3", "region" to "us-east-1"),
                )
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(agent = agent)
                }
            }
            onNodeWithText("version").assertIsDisplayed()
            onNodeWithText("1.2.3").assertIsDisplayed()
            onNodeWithText("region").assertIsDisplayed()
            onNodeWithText("us-east-1").assertIsDisplayed()
        }
}
