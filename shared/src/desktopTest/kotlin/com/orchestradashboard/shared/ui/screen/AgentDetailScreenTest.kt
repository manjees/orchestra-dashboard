package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.PipelineStep
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.repository.EventRepository
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import com.orchestradashboard.shared.domain.usecase.ObservePipelineRunsUseCase
import com.orchestradashboard.shared.ui.agentdetail.AgentDetailViewModel
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

private val testAgent =
    Agent(
        id = "agent-1",
        name = "TestWorker",
        type = Agent.AgentType.WORKER,
        status = Agent.AgentStatus.RUNNING,
        lastHeartbeat = 1000L,
        metadata = mapOf("version" to "2.0"),
    )

private val testPipelineRuns =
    listOf(
        PipelineRun(
            id = "run-1",
            agentId = "agent-1",
            pipelineName = "deploy-pipeline",
            status = PipelineRunStatus.PASSED,
            steps =
                listOf(
                    PipelineStep("compile", StepStatus.PASSED, "OK", 1200L),
                    PipelineStep("test", StepStatus.PASSED, "OK", 3400L),
                ),
            startedAt = 1000L,
            finishedAt = 5600L,
            triggerInfo = "manual",
        ),
    )

private val testEvents =
    listOf(
        AgentEvent(
            id = "evt-1",
            agentId = "agent-1",
            type = EventType.STATUS_CHANGE,
            payload = """{"from":"IDLE","to":"RUNNING"}""",
            timestamp = 2000L,
        ),
    )

private class StaticAgentRepository : AgentRepository {
    override fun observeAgents(): Flow<List<Agent>> = flowOf(listOf(testAgent))

    override fun observeAgent(agentId: String): Flow<Agent> = flowOf(testAgent)

    override suspend fun getAgent(agentId: String): Result<Agent> = Result.success(testAgent)

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus) = Result.success(emptyList<Agent>())

    override suspend fun registerAgent(agent: Agent) = Result.success(agent)

    override suspend fun deregisterAgent(agentId: String) = Result.success(Unit)

    override fun observeAgents(
        page: Int,
        pageSize: Int,
    ): Flow<PagedResult<Agent>> = flowOf(PagedResult(listOf(testAgent), 0, pageSize, 1, 1))

    override suspend fun invalidateCache() {}
}

private class StaticPipelineRepository : PipelineRepository {
    override fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>> = flowOf(testPipelineRuns)

    override suspend fun getPipelineRun(runId: String) = Result.success(testPipelineRuns.first())

    override fun observeActivePipelines(): Flow<List<PipelineRun>> = flowOf(emptyList())
}

private class StaticEventRepository : EventRepository {
    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> = flowOf(testEvents)

    override suspend fun getRecentEvents(limit: Int) = Result.success(testEvents)
}

private class EmptyPipelineRepository : PipelineRepository {
    override fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>> = flowOf(emptyList())

    override suspend fun getPipelineRun(runId: String) = Result.failure<PipelineRun>(NoSuchElementException())

    override fun observeActivePipelines(): Flow<List<PipelineRun>> = flowOf(emptyList())
}

private class EmptyEventRepository : EventRepository {
    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> = flowOf(emptyList())

    override suspend fun getRecentEvents(limit: Int) = Result.success(emptyList<AgentEvent>())
}

private fun createViewModel(
    agentRepository: AgentRepository = StaticAgentRepository(),
    pipelineRepository: PipelineRepository = StaticPipelineRepository(),
    eventRepository: EventRepository = StaticEventRepository(),
): AgentDetailViewModel =
    AgentDetailViewModel(
        agentId = "agent-1",
        getAgentUseCase = GetAgentUseCase(agentRepository),
        observePipelineRunsUseCase = ObservePipelineRunsUseCase(pipelineRepository),
        observeEventsUseCase = ObserveEventsUseCase(eventRepository),
    )

@OptIn(ExperimentalTestApi::class)
class AgentDetailScreenTest {
    @Test
    fun `should display agent name in toolbar`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("TestWorker (worker)").assertIsDisplayed()
        }

    @Test
    fun `should display tab row with overview pipelines and events`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            onNodeWithText("Overview").assertIsDisplayed()
            onNodeWithText("Pipelines").assertIsDisplayed()
            onNodeWithText("Events").assertIsDisplayed()
        }

    @Test
    fun `should display agent overview panel by default`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("TestWorker").assertIsDisplayed()
            onNodeWithText("RUNNING").assertIsDisplayed()
            onNodeWithText("Healthy").assertIsDisplayed()
        }

    @Test
    fun `should switch to pipelines tab and show pipeline run`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("Pipelines").performClick()
            waitForIdle()
            onNodeWithText("deploy-pipeline").assertIsDisplayed()
        }

    @Test
    fun `should switch to events tab and show event`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("Events").performClick()
            waitForIdle()
            onNodeWithText("STATUS CHANGE").assertIsDisplayed()
        }

    @Test
    fun `should show empty state when no pipelines exist`() =
        runComposeUiTest {
            val viewModel = createViewModel(pipelineRepository = EmptyPipelineRepository())
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("Pipelines").performClick()
            waitForIdle()
            onNodeWithText("No pipeline runs.").assertIsDisplayed()
        }

    @Test
    fun `should show empty state when no events exist`() =
        runComposeUiTest {
            val viewModel = createViewModel(eventRepository = EmptyEventRepository())
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("Events").performClick()
            waitForIdle()
            onNodeWithText("No events.").assertIsDisplayed()
        }

    @Test
    fun `should display metadata in overview`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("Metadata").assertIsDisplayed()
            onNodeWithText("version").assertIsDisplayed()
            onNodeWithText("2.0").assertIsDisplayed()
        }
}
