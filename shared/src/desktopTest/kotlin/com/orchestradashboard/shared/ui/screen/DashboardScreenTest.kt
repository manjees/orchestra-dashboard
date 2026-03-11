package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import com.orchestradashboard.shared.ui.TestAgentFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

class FakeAgentRepository(
    private val agents: List<Agent> = emptyList(),
) : AgentRepository {
    override fun observeAgents(): Flow<List<Agent>> = flowOf(agents)

    override suspend fun getAgent(agentId: String): Result<Agent> =
        agents.find { it.id == agentId }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException())

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus): Result<List<Agent>> =
        Result.success(agents.filter { it.status == status })

    override suspend fun registerAgent(agent: Agent): Result<Agent> = Result.success(agent)

    override suspend fun deregisterAgent(agentId: String): Result<Unit> = Result.success(Unit)
}

private fun createViewModel(agents: List<Agent> = emptyList()): DashboardViewModel {
    val repo = FakeAgentRepository(agents)
    return DashboardViewModel(
        observeAgentsUseCase = ObserveAgentsUseCase(repo),
        getAgentUseCase = GetAgentUseCase(repo),
    )
}

@OptIn(ExperimentalTestApi::class)
class DashboardScreenTest {
    @Test
    fun `should display toolbar title`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    DashboardScreen(viewModel = viewModel)
                }
            }
            onNodeWithText("Orchestra Dashboard").assertIsDisplayed()
        }

    @Test
    fun `should display empty state when no agents exist`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    DashboardScreen(viewModel = viewModel)
                }
            }
            waitForIdle()
            onNodeWithText("No agents found.").assertIsDisplayed()
        }

    @Test
    fun `should display agent cards when agents exist`() =
        runComposeUiTest {
            val agents = TestAgentFactory.createList()
            val viewModel = createViewModel(agents)
            setContent {
                DashboardTheme {
                    DashboardScreen(viewModel = viewModel)
                }
            }
            waitForIdle()
            onNodeWithText("orchestrator-1").assertIsDisplayed()
            onNodeWithText("worker-1").assertIsDisplayed()
        }

    @Test
    fun `should show status filter bar`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    DashboardScreen(viewModel = viewModel)
                }
            }
            onNodeWithText("All").assertIsDisplayed()
            onNodeWithText("Running").assertIsDisplayed()
        }
}
