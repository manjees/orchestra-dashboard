package com.orchestradashboard.shared.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.AgentDetailViewModel
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.repository.EventRepository
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

private class NavFakeAgentRepository : AgentRepository {
    override fun observeAgents() = flowOf(emptyList<Agent>())

    override suspend fun getAgent(agentId: String) = Result.failure<Agent>(NoSuchElementException())

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus) = Result.success(emptyList<Agent>())

    override suspend fun registerAgent(agent: Agent) = Result.success(agent)

    override suspend fun deregisterAgent(agentId: String) = Result.success(Unit)
}

private class NavFakePipelineRepository : PipelineRepository {
    override fun observePipelineRuns(agentId: String) = flowOf(emptyList<PipelineRun>())

    override suspend fun getPipelineRun(runId: String) = Result.failure<PipelineRun>(NoSuchElementException())

    override fun observeActivePipelines() = flowOf(emptyList<PipelineRun>())
}

private class NavFakeEventRepository : EventRepository {
    override fun observeEvents(agentId: String) = flowOf(emptyList<AgentEvent>())

    override suspend fun getRecentEvents(limit: Int) = Result.success(emptyList<AgentEvent>())
}

private fun createDashboardViewModel(): DashboardViewModel {
    val repo = NavFakeAgentRepository()
    return DashboardViewModel(ObserveAgentsUseCase(repo), GetAgentUseCase(repo))
}

private fun createDetailViewModel(agentId: String): AgentDetailViewModel {
    val agentRepo = NavFakeAgentRepository()
    val pipelineRepo = NavFakePipelineRepository()
    val eventRepo = NavFakeEventRepository()
    return AgentDetailViewModel(agentId, GetAgentUseCase(agentRepo), pipelineRepo, ObserveEventsUseCase(eventRepo))
}

@OptIn(ExperimentalTestApi::class)
class AppNavigatorTest {
    @Test
    fun `shows dashboard screen for Dashboard state`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AppNavigator(
                        navigationState = NavigationState.Dashboard,
                        dashboardViewModel = createDashboardViewModel(),
                        agentDetailViewModelFactory = ::createDetailViewModel,
                        onNavigate = {},
                    )
                }
            }
            waitForIdle()
            onNodeWithText("Orchestra Dashboard").assertIsDisplayed()
        }

    @Test
    fun `shows detail screen for AgentDetail state`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    AppNavigator(
                        navigationState = NavigationState.AgentDetail("agent-1"),
                        dashboardViewModel = createDashboardViewModel(),
                        agentDetailViewModelFactory = ::createDetailViewModel,
                        onNavigate = {},
                    )
                }
            }
            waitForIdle()
            onNodeWithText("Agent Detail").assertIsDisplayed()
        }

    @Test
    fun `back from detail returns to dashboard`() =
        runComposeUiTest {
            var currentState: NavigationState by mutableStateOf(NavigationState.AgentDetail("agent-1"))
            setContent {
                DashboardTheme {
                    AppNavigator(
                        navigationState = currentState,
                        dashboardViewModel = createDashboardViewModel(),
                        agentDetailViewModelFactory = ::createDetailViewModel,
                        onNavigate = { currentState = it },
                    )
                }
            }
            waitForIdle()
            onNodeWithContentDescription("Back").performClick()
            waitForIdle()
            onNodeWithText("Orchestra Dashboard").assertIsDisplayed()
        }
}
