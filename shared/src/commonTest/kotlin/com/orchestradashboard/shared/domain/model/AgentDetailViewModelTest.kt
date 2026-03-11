package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.repository.EventRepository
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
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

    private val testAgent =
        Agent(
            id = "agent-1",
            name = "Alpha",
            type = Agent.AgentType.WORKER,
            status = Agent.AgentStatus.RUNNING,
            lastHeartbeat = 1000L,
            metadata = mapOf("version" to "1.0"),
        )

    private val testPipelineRuns =
        listOf(
            PipelineRun(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "build",
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
            AgentEvent(
                id = "evt-2",
                agentId = "agent-1",
                type = EventType.HEARTBEAT,
                payload = "{}",
                timestamp = 3000L,
            ),
        )

    private lateinit var agentRepository: FakeAgentDetailRepository
    private lateinit var pipelineRepository: FakePipelineDetailRepository
    private lateinit var eventRepository: FakeEventDetailRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        agentRepository = FakeAgentDetailRepository()
        pipelineRepository = FakePipelineDetailRepository()
        eventRepository = FakeEventDetailRepository()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(agentId: String = "agent-1"): AgentDetailViewModel =
        AgentDetailViewModel(
            agentId = agentId,
            agentRepository = agentRepository,
            pipelineRepository = pipelineRepository,
            eventRepository = eventRepository,
        )

    // --- Agent Loading via Flow ---

    @Test
    fun `should show loading state initially`() =
        runTest {
            val viewModel = createViewModel()

            assertTrue(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.agent)
        }

    @Test
    fun `should load agent via observeAgent flow`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.agent)
            assertEquals("Alpha", state.agent?.name)
        }

    @Test
    fun `should update agent when flow emits new value`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()
            assertEquals(Agent.AgentStatus.RUNNING, viewModel.uiState.value.agent?.status)

            val updatedAgent = testAgent.copy(status = Agent.AgentStatus.IDLE)
            agentRepository.agentFlow.emit(updatedAgent)
            advanceUntilIdle()

            assertEquals(Agent.AgentStatus.IDLE, viewModel.uiState.value.agent?.status)
        }

    // --- Gated Pipeline/Event Observation ---

    @Test
    fun `should start observing pipelines after agent loads`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()

            pipelineRepository.pipelineRunsFlow.emit(testPipelineRuns)
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.pipelineRuns.size)
            assertEquals("build", viewModel.uiState.value.pipelineRuns.first().pipelineName)
        }

    @Test
    fun `should start observing events after agent loads`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()

            eventRepository.eventsFlow.emit(testEvents)
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.events.size)
        }

    // --- Tab Switching ---

    @Test
    fun `should default to overview tab`() =
        runTest {
            val viewModel = createViewModel()

            assertEquals(DetailTab.OVERVIEW, viewModel.uiState.value.selectedTab)
        }

    @Test
    fun `should switch tabs`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectTab(DetailTab.PIPELINES)
            assertEquals(DetailTab.PIPELINES, viewModel.uiState.value.selectedTab)

            viewModel.selectTab(DetailTab.EVENTS)
            assertEquals(DetailTab.EVENTS, viewModel.uiState.value.selectedTab)

            viewModel.selectTab(DetailTab.OVERVIEW)
            assertEquals(DetailTab.OVERVIEW, viewModel.uiState.value.selectedTab)
        }

    // --- Error Handling ---

    @Test
    fun `should set error when agent observation fails`() =
        runTest {
            agentRepository.shouldFailObserve = true

            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.error)
        }

    @Test
    fun `should keep agent data when pipeline observation fails`() =
        runTest {
            pipelineRepository.shouldFail = true

            val viewModel = createViewModel()
            viewModel.startObserving()

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.agent)
            assertEquals("Alpha", state.agent?.name)
            assertTrue(state.pipelineRuns.isEmpty())
        }

    @Test
    fun `should keep agent data when event observation fails`() =
        runTest {
            eventRepository.shouldFail = true

            val viewModel = createViewModel()
            viewModel.startObserving()

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.agent)
            assertEquals("Alpha", state.agent?.name)
            assertTrue(state.events.isEmpty())
        }

    // --- Missing Agent ---

    @Test
    fun `should handle missing agent gracefully`() =
        runTest {
            agentRepository.observeError = NoSuchElementException("Agent not found")
            agentRepository.shouldFailObserve = true

            val viewModel = createViewModel("nonexistent")
            viewModel.startObserving()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNull(state.agent)
            assertNotNull(state.error)
        }

    // --- Cleanup ---

    @Test
    fun `should clear error`() =
        runTest {
            agentRepository.shouldFailObserve = true

            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `should cancel scope on onCleared`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()

            viewModel.onCleared()
            // Should not crash after clearing
        }

    @Test
    fun `should not start duplicate observations when startObserving called twice`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            viewModel.startObserving() // second call should be a no-op

            agentRepository.agentFlow.emit(testAgent)
            advanceUntilIdle()

            // Only one observation started — agent is still loaded correctly
            assertNotNull(viewModel.uiState.value.agent)
            assertEquals("Alpha", viewModel.uiState.value.agent?.name)
        }
}

// ─── Test Fakes ──────────────────────────────────────────────────────────

private class FakeAgentDetailRepository : AgentRepository {
    val agentFlow = MutableSharedFlow<Agent>(replay = 1)
    var shouldFailObserve = false
    var observeError: Throwable = RuntimeException("Connection failed")

    override fun observeAgents(): Flow<List<Agent>> = flow { throw NotImplementedError() }

    override fun observeAgent(agentId: String): Flow<Agent> {
        if (shouldFailObserve) return flow { throw observeError }
        return agentFlow
    }

    override suspend fun getAgent(agentId: String): Result<Agent> = Result.failure(NotImplementedError())

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus) = Result.failure<List<Agent>>(NotImplementedError())

    override suspend fun registerAgent(agent: Agent) = Result.failure<Agent>(NotImplementedError())

    override suspend fun deregisterAgent(agentId: String) = Result.failure<Unit>(NotImplementedError())
}

private class FakePipelineDetailRepository : PipelineRepository {
    val pipelineRunsFlow = MutableSharedFlow<List<PipelineRun>>(replay = 1)
    var shouldFail = false

    override fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>> {
        if (shouldFail) return flow { throw RuntimeException("Pipeline fetch failed") }
        return pipelineRunsFlow
    }

    override suspend fun getPipelineRun(runId: String): Result<PipelineRun> = Result.failure(NotImplementedError())

    override fun observeActivePipelines(): Flow<List<PipelineRun>> = flow { throw NotImplementedError() }
}

private class FakeEventDetailRepository : EventRepository {
    val eventsFlow = MutableSharedFlow<List<AgentEvent>>(replay = 1)
    var shouldFail = false

    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> {
        if (shouldFail) return flow { throw RuntimeException("Event fetch failed") }
        return eventsFlow
    }

    override suspend fun getRecentEvents(limit: Int): Result<List<AgentEvent>> = Result.failure(NotImplementedError())
}
