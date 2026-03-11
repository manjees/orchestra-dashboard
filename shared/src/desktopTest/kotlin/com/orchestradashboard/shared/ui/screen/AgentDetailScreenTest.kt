package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.AgentDetailViewModel
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.PipelineStep
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.domain.repository.EventRepository
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

private class DetailFakeAgentRepository(
    private val agent: Agent? = null,
) : com.orchestradashboard.shared.domain.repository.AgentRepository {
    override fun observeAgents() = flowOf(listOfNotNull(agent))

    override suspend fun getAgent(agentId: String) = agent?.let { Result.success(it) } ?: Result.failure(NoSuchElementException())

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus) = Result.success(emptyList<Agent>())

    override suspend fun registerAgent(agent: Agent) = Result.success(agent)

    override suspend fun deregisterAgent(agentId: String) = Result.success(Unit)
}

private class DetailFakePipelineRepository(
    private val runs: List<PipelineRun> = emptyList(),
) : PipelineRepository {
    override fun observePipelineRuns(agentId: String) = flowOf(runs)

    override suspend fun getPipelineRun(runId: String) = Result.failure<PipelineRun>(NoSuchElementException())

    override fun observeActivePipelines() = flowOf(emptyList<PipelineRun>())
}

private class DetailFakeEventRepository(
    private val events: List<AgentEvent> = emptyList(),
) : EventRepository {
    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> = flowOf(events)

    override suspend fun getRecentEvents(limit: Int) = Result.success(events)
}

private val testAgent = Agent("agent-1", "Test Agent", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 1000L)
private val testRuns =
    listOf(
        PipelineRun(
            "run-1",
            "agent-1",
            "Build",
            PipelineRunStatus.PASSED,
            listOf(PipelineStep("Compile", StepStatus.PASSED, "", 5000L)),
            1000L,
            6000L,
            "manual",
        ),
    )
private val testEvents =
    listOf(
        AgentEvent("evt-1", "agent-1", EventType.HEARTBEAT, "{}", 2000L),
    )

private fun createViewModel(
    agent: Agent? = testAgent,
    runs: List<PipelineRun> = testRuns,
    events: List<AgentEvent> = testEvents,
): AgentDetailViewModel {
    val agentRepo = DetailFakeAgentRepository(agent)
    val pipelineRepo = DetailFakePipelineRepository(runs)
    val eventRepo = DetailFakeEventRepository(events)
    return AgentDetailViewModel(
        agentId = "agent-1",
        getAgentUseCase = GetAgentUseCase(agentRepo),
        pipelineRepository = pipelineRepo,
        observeEventsUseCase = ObserveEventsUseCase(eventRepo),
    )
}

@OptIn(ExperimentalTestApi::class)
class AgentDetailScreenTest {
    @Test
    fun `displays fallback title before agent loads`() =
        runComposeUiTest {
            val viewModel = createViewModel(agent = null)
            setContent { DashboardTheme { AgentDetailScreen(viewModel = viewModel, onBack = {}) } }
            waitForIdle()
            onNodeWithText("Agent Detail").assertIsDisplayed()
        }

    @Test
    fun `displays three tabs overview pipelines events`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent { DashboardTheme { AgentDetailScreen(viewModel = viewModel, onBack = {}) } }
            waitForIdle()
            onNodeWithText("Overview").assertIsDisplayed()
            onNodeWithText("Pipelines").assertIsDisplayed()
            onNodeWithText("Events").assertIsDisplayed()
        }

    @Test
    fun `clicking pipelines tab shows pipeline list`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent { DashboardTheme { AgentDetailScreen(viewModel = viewModel, onBack = {}) } }
            waitForIdle()
            onNodeWithText("Pipelines").performClick()
            waitForIdle()
            onNodeWithText("Build").assertIsDisplayed()
        }

    @Test
    fun `clicking events tab shows event feed`() =
        runComposeUiTest {
            val viewModel = createViewModel()
            setContent { DashboardTheme { AgentDetailScreen(viewModel = viewModel, onBack = {}) } }
            waitForIdle()
            onNodeWithText("Events").performClick()
            waitForIdle()
            onNodeWithText("HB").assertIsDisplayed()
        }

    @Test
    fun `back button calls onBack callback`() =
        runComposeUiTest {
            var backCalled = false
            val viewModel = createViewModel()
            setContent {
                DashboardTheme {
                    AgentDetailScreen(viewModel = viewModel, onBack = { backCalled = true })
                }
            }
            waitForIdle()
            onNodeWithContentDescription("Back").performClick()
            waitForIdle()
            assert(backCalled)
        }

    @Test
    fun `empty pipeline list shows empty state`() =
        runComposeUiTest {
            val viewModel = createViewModel(runs = emptyList())
            setContent { DashboardTheme { AgentDetailScreen(viewModel = viewModel, onBack = {}) } }
            waitForIdle()
            onNodeWithText("Pipelines").performClick()
            waitForIdle()
            onNodeWithText("No pipeline runs.").assertIsDisplayed()
        }

    @Test
    fun `empty event list shows empty state`() =
        runComposeUiTest {
            val viewModel = createViewModel(events = emptyList())
            setContent { DashboardTheme { AgentDetailScreen(viewModel = viewModel, onBack = {}) } }
            waitForIdle()
            onNodeWithText("Events").performClick()
            waitForIdle()
            onNodeWithText("No events recorded.").assertIsDisplayed()
        }
}
