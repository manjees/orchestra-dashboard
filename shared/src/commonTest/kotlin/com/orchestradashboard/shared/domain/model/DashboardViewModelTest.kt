package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class DashboardViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val testAgents =
        listOf(
            Agent(id = "1", name = "Agent-1", type = Agent.AgentType.WORKER, status = Agent.AgentStatus.RUNNING, lastHeartbeat = 1000L),
            Agent(id = "2", name = "Agent-2", type = Agent.AgentType.ORCHESTRATOR, status = Agent.AgentStatus.IDLE, lastHeartbeat = 2000L),
            Agent(id = "3", name = "Agent-3", type = Agent.AgentType.REVIEWER, status = Agent.AgentStatus.ERROR, lastHeartbeat = 3000L),
        )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        agents: List<Agent> = testAgents,
        shouldFail: Boolean = false,
        errorMessage: String = "Repository error",
    ): DashboardViewModel {
        val repository = TestAgentRepository(agents, shouldFail, errorMessage)
        return DashboardViewModel(
            observeAgentsUseCase = ObserveAgentsUseCase(repository),
            getAgentUseCase = GetAgentUseCase(repository),
        )
    }

    @Test
    fun `initial state is not loading with empty agents`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.agents.isEmpty())
    }

    @Test
    fun `startObserving sets isLoading true then populates agents`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()
            val state = viewModel.uiState.value
            assertEquals(false, state.isLoading)
            assertEquals(3, state.agents.size)
            viewModel.onCleared()
        }

    @Test
    fun `startObserving sets error state on repository failure`() =
        runTest {
            val viewModel = createViewModel(shouldFail = true, errorMessage = "Network error")
            viewModel.startObserving()
            advanceUntilIdle()
            val state = viewModel.uiState.value
            assertNotNull(state.error)
            assertEquals("Network error", state.error)
            assertEquals(false, state.isLoading)
            viewModel.onCleared()
        }

    @Test
    fun `setFilter updates filter in state`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()
            viewModel.setFilter(Agent.AgentStatus.RUNNING)
            val state = viewModel.uiState.value
            assertEquals(Agent.AgentStatus.RUNNING, state.filter)
            viewModel.onCleared()
        }

    @Test
    fun `setFilter with null clears filter`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.setFilter(Agent.AgentStatus.RUNNING)
            viewModel.setFilter(null)
            val state = viewModel.uiState.value
            assertNull(state.filter)
            viewModel.onCleared()
        }

    @Test
    fun `selectAgent sets selectedAgent on success`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()
            viewModel.selectAgent("1")
            advanceUntilIdle()
            val state = viewModel.uiState.value
            assertNotNull(state.selectedAgent)
            assertEquals("1", state.selectedAgent?.id)
            viewModel.onCleared()
        }

    @Test
    fun `selectAgent with null clears selection`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()
            viewModel.selectAgent("1")
            advanceUntilIdle()
            viewModel.selectAgent(null)
            assertNull(viewModel.uiState.value.selectedAgent)
            viewModel.onCleared()
        }

    @Test
    fun `selectAgent sets error on failure`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.selectAgent("nonexistent")
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)
            viewModel.onCleared()
        }

    @Test
    fun `clearError resets error to null`() =
        runTest {
            val viewModel = createViewModel(shouldFail = true)
            viewModel.startObserving()
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)
            viewModel.clearError()
            assertNull(viewModel.uiState.value.error)
            viewModel.onCleared()
        }

    @Test
    fun `onCleared cancels scope`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onCleared()
            viewModel.startObserving()
            advanceUntilIdle()
            // After onCleared, startObserving should not update state (scope cancelled)
            assertTrue(viewModel.uiState.value.agents.isEmpty())
        }
}

/** Test double for AgentRepository with configurable error behavior */
private class TestAgentRepository(
    private val agents: List<Agent> = emptyList(),
    private val shouldFail: Boolean = false,
    private val errorMessage: String = "Repository error",
) : AgentRepository {
    override fun observeAgents(): Flow<List<Agent>> =
        if (shouldFail) {
            flow { throw RuntimeException(errorMessage) }
        } else {
            flowOf(agents)
        }

    override suspend fun getAgent(agentId: String): Result<Agent> =
        agents.find { it.id == agentId }
            ?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Agent $agentId not found"))

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus): Result<List<Agent>> =
        Result.success(agents.filter { it.status == status })

    override suspend fun registerAgent(agent: Agent): Result<Agent> = Result.success(agent)

    override suspend fun deregisterAgent(agentId: String): Result<Unit> = Result.success(Unit)
}
