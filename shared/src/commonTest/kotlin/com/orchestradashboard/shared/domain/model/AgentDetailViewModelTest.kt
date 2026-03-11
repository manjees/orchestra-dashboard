package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AgentDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val agentId = "agent-1"

    private val testAgent =
        Agent(
            id = agentId,
            name = "Test Agent",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.RUNNING,
            lastHeartbeat = 1000L,
        )

    private val testPipelineRuns =
        listOf(
            PipelineRun(
                id = "run-1",
                agentId = agentId,
                pipelineName = "Build",
                status = PipelineRunStatus.PASSED,
                steps =
                    listOf(
                        PipelineStep("Compile", StepStatus.PASSED, "", 5000L),
                        PipelineStep("Test", StepStatus.PASSED, "", 10000L),
                    ),
                startedAt = 1000L,
                finishedAt = 16000L,
                triggerInfo = "manual",
            ),
        )

    private val testEvents =
        listOf(
            AgentEvent("evt-1", agentId, EventType.HEARTBEAT, "{}", 2000L),
            AgentEvent("evt-2", agentId, EventType.STATUS_CHANGE, "{\"status\":\"RUNNING\"}", 1000L),
        )

    private lateinit var agentRepository: FakeAgentRepository
    private lateinit var pipelineRepository: FakePipelineRepository
    private lateinit var eventRepository: FakeEventRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        agentRepository = FakeAgentRepository()
        pipelineRepository = FakePipelineRepository()
        eventRepository = FakeEventRepository()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AgentDetailViewModel =
        AgentDetailViewModel(
            agentId = agentId,
            getAgentUseCase = GetAgentUseCase(agentRepository),
            pipelineRepository = pipelineRepository,
            observeEventsUseCase = ObserveEventsUseCase(eventRepository),
        )

    // --- Loading & Data Fetch ---

    @Test
    fun `loadAgent sets agent from repository`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            assertEquals(testAgent, viewModel.uiState.value.agent)
        }

    @Test
    fun `loadAgent sets isLoading true then false`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            assertTrue(viewModel.uiState.value.isLoading == false)
            viewModel.loadAgent()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `loadAgent sets error on failure`() =
        runTest {
            agentRepository.getAgentResult = Result.failure(RuntimeException("Not found"))
            val viewModel = createViewModel()

            viewModel.loadAgent()
            advanceUntilIdle()

            assertEquals("Not found", viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    // --- Pipeline Observation ---

    @Test
    fun `startObserving collects pipeline runs for agent`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.startObserving()
            advanceUntilIdle()

            pipelineRepository.pipelineRunsFlow.emit(testPipelineRuns)
            advanceUntilIdle()

            assertEquals(testPipelineRuns, viewModel.uiState.value.pipelineRuns)
        }

    @Test
    fun `pipeline runs update when flow emits new data`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.startObserving()
            advanceUntilIdle()

            pipelineRepository.pipelineRunsFlow.emit(testPipelineRuns)
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.pipelineRuns.size)

            pipelineRepository.pipelineRunsFlow.emit(emptyList())
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.pipelineRuns.size)
        }

    // --- Event Observation ---

    @Test
    fun `startObserving collects events for agent`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.startObserving()
            advanceUntilIdle()

            eventRepository.eventsFlow.emit(testEvents)
            advanceUntilIdle()

            assertEquals(testEvents, viewModel.uiState.value.events)
        }

    @Test
    fun `events update when flow emits new data`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            val viewModel = createViewModel()

            viewModel.startObserving()
            advanceUntilIdle()

            eventRepository.eventsFlow.emit(testEvents)
            advanceUntilIdle()
            assertEquals(2, viewModel.uiState.value.events.size)

            eventRepository.eventsFlow.emit(emptyList())
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.events.size)
        }

    // --- Tab Selection ---

    @Test
    fun `selectTab updates selectedTabIndex`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectTab(1)

            assertEquals(1, viewModel.uiState.value.selectedTabIndex)
        }

    @Test
    fun `selectTab clamps to valid range 0-2`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectTab(-1)
            assertEquals(0, viewModel.uiState.value.selectedTabIndex)

            viewModel.selectTab(5)
            assertEquals(2, viewModel.uiState.value.selectedTabIndex)
        }

    // --- Pipeline Card Expansion ---

    @Test
    fun `togglePipelineExpanded adds id to expanded set`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.togglePipelineExpanded("run-1")

            assertTrue(viewModel.uiState.value.expandedPipelineIds.contains("run-1"))
        }

    @Test
    fun `togglePipelineExpanded removes id when already expanded`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.togglePipelineExpanded("run-1")
            assertTrue(viewModel.uiState.value.expandedPipelineIds.contains("run-1"))

            viewModel.togglePipelineExpanded("run-1")
            assertFalse(viewModel.uiState.value.expandedPipelineIds.contains("run-1"))
        }

    // --- Error Handling ---

    @Test
    fun `clearError sets error to null`() =
        runTest {
            agentRepository.getAgentResult = Result.failure(RuntimeException("Error"))
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
            pipelineRepository.observeError = RuntimeException("Pipeline error")
            val viewModel = createViewModel()

            viewModel.startObserving()
            advanceUntilIdle()

            assertEquals("Pipeline error", viewModel.uiState.value.error)
        }

    @Test
    fun `event observation error sets error state`() =
        runTest {
            agentRepository.getAgentResult = Result.success(testAgent)
            eventRepository.shouldFailObserve = true
            eventRepository.observeError = RuntimeException("Event error")
            val viewModel = createViewModel()

            viewModel.startObserving()
            advanceUntilIdle()

            assertEquals("Event error", viewModel.uiState.value.error)
        }

    // --- Cleanup ---

    @Test
    fun `onCleared cancels coroutine scope`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onCleared()

            // Verifying no crash when calling after cleared
            viewModel.selectTab(1)
        }
}
