package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private lateinit var fakeRepo: FakeAgentRepository
    private lateinit var viewModel: DashboardViewModel

    private val testAgents =
        listOf(
            Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
            Agent("2", "Beta", Agent.AgentType.PLANNER, Agent.AgentStatus.IDLE, 200L),
            Agent("3", "Gamma", Agent.AgentType.REVIEWER, Agent.AgentStatus.ERROR, 300L),
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepo = FakeAgentRepository()
        viewModel =
            DashboardViewModel(
                observeAgentsUseCase = ObserveAgentsUseCase(fakeRepo),
                getAgentUseCase = GetAgentUseCase(fakeRepo),
            )
    }

    @AfterTest
    fun tearDown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value

        assertTrue(state.agents.isEmpty())
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertNull(state.selectedAgent)
    }

    @Test
    fun `startObserving sets isLoading then populates agents on emission`() =
        runTest {
            viewModel.startObserving()
            assertEquals(true, viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.error)

            fakeRepo.agentsFlow.emit(testAgents)

            val state = viewModel.uiState.value
            assertEquals(testAgents, state.agents)
            assertEquals(false, state.isLoading)
            assertEquals(ConnectionStatus.CONNECTED, state.connectionStatus)
        }

    @Test
    fun `error state on repository failure`() =
        runTest {
            fakeRepo.shouldFailObserve = true

            viewModel.startObserving()

            val state = viewModel.uiState.value
            assertEquals("Connection failed", state.error)
            assertEquals(false, state.isLoading)
            assertEquals(ConnectionStatus.DISCONNECTED, state.connectionStatus)
        }

    @Test
    fun `selectAgent sets selectedAgent on success`() =
        runTest {
            val agent = testAgents[0]
            fakeRepo.getAgentResult = Result.success(agent)

            viewModel.selectAgent("1")

            assertEquals(agent, viewModel.uiState.value.selectedAgent)
        }

    @Test
    fun `selectAgent with null clears selectedAgent`() {
        viewModel.selectAgent(null)

        assertNull(viewModel.uiState.value.selectedAgent)
    }

    @Test
    fun `selectAgent sets error on failure`() =
        runTest {
            fakeRepo.getAgentResult = Result.failure(RuntimeException("Not found"))

            viewModel.selectAgent("999")

            assertEquals("Not found", viewModel.uiState.value.error)
        }

    @Test
    fun `selectAgent sets fallback error message when exception has no message`() =
        runTest {
            fakeRepo.getAgentResult = Result.failure(object : Throwable() { override val message: String? = null })

            viewModel.selectAgent("1")

            assertEquals("Unknown error", viewModel.uiState.value.error)
        }

    @Test
    fun `setFilter updates filter in state`() {
        viewModel.setFilter(Agent.AgentStatus.RUNNING)

        assertEquals(Agent.AgentStatus.RUNNING, viewModel.uiState.value.filter)
    }

    @Test
    fun `setFilter with null clears filter`() {
        viewModel.setFilter(Agent.AgentStatus.RUNNING)
        viewModel.setFilter(null)

        assertNull(viewModel.uiState.value.filter)
    }

    @Test
    fun `clearError resets error to null`() =
        runTest {
            fakeRepo.getAgentResult = Result.failure(RuntimeException("Some error"))
            viewModel.selectAgent("1")
            assertEquals("Some error", viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `onCleared cancels scope without crash`() {
        viewModel.onCleared()
        viewModel.startObserving()
    }
}
