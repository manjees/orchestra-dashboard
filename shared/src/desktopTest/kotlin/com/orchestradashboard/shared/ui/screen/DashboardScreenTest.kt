package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.assertTrue
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.domain.model.Metric
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.repository.MetricRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.GetAggregatedMetricsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import com.orchestradashboard.shared.ui.TestAgentFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

class FakeAgentRepository(
    private val agents: List<Agent> = emptyList(),
) : AgentRepository {
    override fun observeAgents(): Flow<List<Agent>> = flowOf(agents)

    override fun observeAgent(agentId: String): Flow<Agent> =
        agents.find { it.id == agentId }
            ?.let { flowOf(it) }
            ?: flow { throw NoSuchElementException("Agent $agentId not found") }

    override suspend fun getAgent(agentId: String): Result<Agent> =
        agents.find { it.id == agentId }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException())

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus): Result<List<Agent>> =
        Result.success(agents.filter { it.status == status })

    override suspend fun registerAgent(agent: Agent): Result<Agent> = Result.success(agent)

    override suspend fun deregisterAgent(agentId: String): Result<Unit> = Result.success(Unit)

    override fun observeAgents(
        page: Int,
        pageSize: Int,
    ): Flow<PagedResult<Agent>> = flowOf(PagedResult(agents, 0, pageSize, agents.size.toLong(), 1))

    override suspend fun invalidateCache() {}
}

class FakeMetricRepository(
    private val aggregatedMetrics: List<Metric> = emptyList(),
) : MetricRepository {
    override fun observeMetrics(agentId: String): Flow<List<Metric>> = flowOf(emptyList())

    override suspend fun getFleetMetrics(): Result<List<Metric>> = Result.success(emptyList())

    override suspend fun getAggregatedMetrics(
        agentId: String,
        timeRange: TimeRange,
    ): Result<List<Metric>> = Result.success(aggregatedMetrics)
}

private fun createViewModel(
    agents: List<Agent> = emptyList(),
    metrics: List<Metric> = emptyList(),
): DashboardViewModel {
    val repo = FakeAgentRepository(agents)
    val metricRepo = FakeMetricRepository(metrics)
    return DashboardViewModel(
        observeAgentsUseCase = ObserveAgentsUseCase(repo),
        getAgentUseCase = GetAgentUseCase(repo),
        getAggregatedMetricsUseCase = GetAggregatedMetricsUseCase(metricRepo),
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

    @Test
    fun `should display View Projects button when callback provided`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    DashboardScreen(viewModel = viewModel, onViewProjectsClick = {})
                }
            }
            onNodeWithText("View Projects").assertIsDisplayed()
        }

    @Test
    fun `View Projects button fires callback on click`() =
        runComposeUiTest {
            var clicked = false
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    DashboardScreen(viewModel = viewModel, onViewProjectsClick = { clicked = true })
                }
            }
            onNodeWithText("View Projects").performClick()
            assertTrue(clicked)
        }

    @Test
    fun `should render chart component with valid time series data`() =
        runComposeUiTest {
            val agents = TestAgentFactory.createList()
            val metrics =
                listOf(
                    Metric(agentId = "1", name = "cpu_usage", value = 42.0, unit = "percent", timestamp = 1000L),
                    Metric(agentId = "1", name = "cpu_usage", value = 58.0, unit = "percent", timestamp = 2000L),
                    Metric(agentId = "1", name = "cpu_usage", value = 35.0, unit = "percent", timestamp = 3000L),
                )
            val viewModel = createViewModel(agents = agents, metrics = metrics)
            setContent {
                DashboardTheme {
                    DashboardScreen(viewModel = viewModel)
                }
            }
            waitForIdle()

            // Select an agent to trigger metrics loading
            onNodeWithText("orchestrator-1").performClick()
            waitForIdle()

            // Chart section should be visible with metric name and time range selector
            onNodeWithText("cpu_usage").assertIsDisplayed()
            onNodeWithText("24h").assertIsDisplayed()
            onNodeWithText("7d").assertIsDisplayed()
            onNodeWithText("30d").assertIsDisplayed()
            onNodeWithTag("metrics_chart").assertIsDisplayed()
        }
}
