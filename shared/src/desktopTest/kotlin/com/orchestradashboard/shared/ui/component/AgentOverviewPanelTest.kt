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
    fun `displays agent name and type`() =
        runComposeUiTest {
            val agent = TestAgentFactory.create(name = "Alpha Agent", type = Agent.AgentType.ORCHESTRATOR)
            setContent { DashboardTheme { AgentOverviewPanel(agent = agent) } }
            onNodeWithText("Alpha Agent").assertIsDisplayed()
            onNodeWithText("orchestrator").assertIsDisplayed()
        }

    @Test
    fun `displays health badge for healthy agent`() =
        runComposeUiTest {
            val agent = TestAgentFactory.create(status = Agent.AgentStatus.RUNNING)
            setContent { DashboardTheme { AgentOverviewPanel(agent = agent) } }
            onNodeWithText("Healthy").assertIsDisplayed()
        }

    @Test
    fun `displays unhealthy badge for error agent`() =
        runComposeUiTest {
            val agent = TestAgentFactory.create(status = Agent.AgentStatus.ERROR)
            setContent { DashboardTheme { AgentOverviewPanel(agent = agent) } }
            onNodeWithText("Unhealthy").assertIsDisplayed()
        }

    @Test
    fun `displays metadata entries`() =
        runComposeUiTest {
            val agent =
                Agent(
                    id = "1",
                    name = "Test",
                    type = Agent.AgentType.WORKER,
                    status = Agent.AgentStatus.RUNNING,
                    lastHeartbeat = 100L,
                    metadata = mapOf("version" to "1.2.3", "region" to "us-east"),
                )
            setContent { DashboardTheme { AgentOverviewPanel(agent = agent) } }
            onNodeWithText("version:").assertIsDisplayed()
            onNodeWithText("1.2.3").assertIsDisplayed()
            onNodeWithText("region:").assertIsDisplayed()
            onNodeWithText("us-east").assertIsDisplayed()
        }

    @Test
    fun `displays unhealthy badge for offline agent`() =
        runComposeUiTest {
            val agent = TestAgentFactory.create(status = Agent.AgentStatus.OFFLINE)
            setContent { DashboardTheme { AgentOverviewPanel(agent = agent) } }
            onNodeWithText("Unhealthy").assertIsDisplayed()
        }
}
