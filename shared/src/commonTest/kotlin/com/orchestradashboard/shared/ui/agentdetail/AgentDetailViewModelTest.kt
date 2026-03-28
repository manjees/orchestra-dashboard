package com.orchestradashboard.shared.ui.agentdetail

import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.CommandType
import com.orchestradashboard.shared.domain.model.EventType
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import com.orchestradashboard.shared.domain.usecase.ObservePipelineRunsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AgentDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val testAgent = Agent("agent-1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L)

    private val testPipelineRuns =
        listOf(
            PipelineRun("run-1", "agent-1", "build", PipelineRunStatus.PASSED, emptyList(), 1000L, 2000L, "manual"),
            PipelineRun("run-2", "agent-1", "deploy", PipelineRunStatus.RUNNING, emptyList(), 3000L, null, "auto"),
        )

    private val testEvents =
        listOf(
            AgentEvent("evt-1", "agent-1", EventType.STATUS_CHANGE, "{}", 1000L),
            AgentEvent("evt-2", "agent-1", EventType.HEARTBEAT, "{}", 2000L),
        )

    private lateinit var agentRepository: FakeAgentRepository
    private lateinit var pipelineRepository: FakePipelineRepository
    private lateinit var eventRepository: FakeEventRepository
    private lateinit var commandRepository: FakeAgentCommandRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        agentRepository = FakeAgentRepository()
        pipelineRepository = FakePipelineRepository()
        eventRepository = FakeEventRepository()
        commandRepository = FakeAgentCommandRepository()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(agentId: String = "agent-1"): AgentDetailViewModel =
        AgentDetailViewModel(
            agentId = agentId,
            getAgentUseCase = GetAgentUseCase(agentRepository),
            observePipelineRunsUseCase = ObservePipelineRunsUseCase(pipelineRepository),
            observeEventsUseCase = ObserveEventsUseCase(eventRepository),
            agentCommandRepository = commandRepository,
        )

    // --- Group 1: Initial State ---

    @Test
    fun `initial state has correct defaults`() =
        runTest {
            val viewModel = createViewModel()

            val state = viewModel.uiState.value
            assertNull(state.agent)
            assertTrue(state.pipelineRuns.isEmpty())
            assertTrue(state.events.isEmpty())
            assertEquals(false, state.isLoading)
            assertNull(state.error)
            assertEquals(DetailTab.OVERVIEW, state.selectedTab)
        }

    // --- Group 2: Agent Loading ---

    @Test
    fun `loadAgent populates agent and clears isLoading`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            assertEquals(testAgent, viewModel.uiState.value.agent)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `loadAgent sets error when agent not found`() =
        runTest {
            agentRepository.getAgentResult = Result.failure(NoSuchElementException("Agent not found"))
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            assertEquals("Agent not found", viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.agent)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }

    @Test
    fun `loadAgent starts observing pipeline runs for agent`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            pipelineRepository.pipelineRunsFlow.emit(testPipelineRuns)
            advanceUntilIdle()

            assertEquals(testPipelineRuns, viewModel.uiState.value.pipelineRuns)
        }

    @Test
    fun `loadAgent starts observing events for agent`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            eventRepository.eventsFlow.emit(testEvents)
            advanceUntilIdle()

            assertEquals(testEvents, viewModel.uiState.value.events)
        }

    // --- Group 3: Tab Switching ---

    @Test
    fun `selectTab updates selectedTab in state`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectTab(DetailTab.PIPELINES)
            assertEquals(DetailTab.PIPELINES, viewModel.uiState.value.selectedTab)

            viewModel.selectTab(DetailTab.EVENTS)
            assertEquals(DetailTab.EVENTS, viewModel.uiState.value.selectedTab)
        }

    @Test
    fun `selectTab to same tab is idempotent`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectTab(DetailTab.OVERVIEW)
            assertEquals(DetailTab.OVERVIEW, viewModel.uiState.value.selectedTab)

            viewModel.selectTab(DetailTab.OVERVIEW)
            assertEquals(DetailTab.OVERVIEW, viewModel.uiState.value.selectedTab)
        }

    // --- Group 4: Error Handling ---

    @Test
    fun `clearError resets error to null`() =
        runTest {
            agentRepository.getAgentResult = Result.failure(NoSuchElementException("Agent not found"))
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `pipeline observation error sets error state`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            pipelineRepository.shouldFailObserve = true
            pipelineRepository.observeError = RuntimeException("Pipeline stream failed")
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertTrue(viewModel.uiState.value.error!!.contains("Pipeline stream failed"))
        }

    @Test
    fun `event observation error sets error state`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            eventRepository.shouldFailObserve = true
            eventRepository.observeError = RuntimeException("Event stream failed")
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertTrue(viewModel.uiState.value.error!!.contains("Event stream failed"))
        }

    @Test
    fun `blank agentId sets error without loading`() =
        runTest {
            val viewModel = createViewModel(agentId = "  ")

            viewModel.loadAgent()
            advanceUntilIdle()

            assertEquals("Invalid agent ID", viewModel.uiState.value.error)
            assertEquals(false, viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.agent)
        }

    // --- Group 5: Command Dispatch ---

    @Test
    fun `sendCommand sets commandInProgress then clears on success`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            viewModel.sendCommand(CommandType.STOP)
            advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.commandInProgress)
            assertTrue(viewModel.uiState.value.commandResult is CommandResult.Success)
        }

    @Test
    fun `sendCommand sets failure result on error`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            commandRepository.sendCommandResult = Result.failure(RuntimeException("Connection failed"))
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            viewModel.sendCommand(CommandType.STOP)
            advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.commandInProgress)
            assertTrue(viewModel.uiState.value.commandResult is CommandResult.Failure)
            assertEquals(
                "Connection failed",
                (viewModel.uiState.value.commandResult as CommandResult.Failure).message,
            )
        }

    @Test
    fun `clearCommandResult resets commandResult to null`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            viewModel.sendCommand(CommandType.STOP)
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.commandResult)

            viewModel.clearCommandResult()
            assertNull(viewModel.uiState.value.commandResult)
        }

    // --- Group 6: Lifecycle ---

    @Test
    fun `onCleared cancels all coroutines`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            pipelineRepository.pipelineRunsFlow.emit(testPipelineRuns)
            advanceUntilIdle()
            assertEquals(testPipelineRuns, viewModel.uiState.value.pipelineRuns)

            viewModel.onCleared()

            pipelineRepository.pipelineRunsFlow.emit(emptyList())
            advanceUntilIdle()

            assertEquals(testPipelineRuns, viewModel.uiState.value.pipelineRuns)
        }
}
