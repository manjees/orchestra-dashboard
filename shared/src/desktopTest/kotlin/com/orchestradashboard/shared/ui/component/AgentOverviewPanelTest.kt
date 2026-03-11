package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.ui.TestAgentFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.datetime.Clock
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
    fun `should display Healthy badge for running agent`() =
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
    fun `should display Unhealthy badge for error agent`() =
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
    fun `should display Healthy badge for idle agent`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent = TestAgentFactory.create(status = Agent.AgentStatus.IDLE),
                    )
                }
            }
            onNodeWithText("Healthy").assertIsDisplayed()
        }

    @Test
    fun `should display Unhealthy badge for offline agent`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent = TestAgentFactory.create(status = Agent.AgentStatus.OFFLINE),
                    )
                }
            }
            onNodeWithText("Unhealthy").assertIsDisplayed()
        }

    @Test
    fun `should display metadata entries`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent =
                            TestAgentFactory.create(
                                metadata = mapOf("version" to "1.2.3", "region" to "us-east-1"),
                            ),
                    )
                }
            }
            onNodeWithText("version").assertIsDisplayed()
            onNodeWithText("1.2.3").assertIsDisplayed()
            onNodeWithText("region").assertIsDisplayed()
            onNodeWithText("us-east-1").assertIsDisplayed()
        }

    @Test
    fun `should display uptime label`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(agent = TestAgentFactory.create())
                }
            }
            onNodeWithText("Uptime").assertIsDisplayed()
        }

    @Test
    fun `should display uptime based on createdAt not lastHeartbeat`() =
        runComposeUiTest {
            val now = Clock.System.now().toEpochMilliseconds()
            val twoHoursAgo = now - 2 * 3600 * 1000
            val fiveMinutesAgo = now - 5 * 60 * 1000
            setContent {
                DashboardTheme {
                    AgentOverviewPanel(
                        agent =
                            TestAgentFactory.create(
                                createdAt = twoHoursAgo,
                                lastHeartbeat = fiveMinutesAgo,
                            ),
                    )
                }
            }
            onNodeWithText("2h 0m").assertIsDisplayed()
        }
}
