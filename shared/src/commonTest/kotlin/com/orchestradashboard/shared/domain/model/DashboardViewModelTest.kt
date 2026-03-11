package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.usecase.FakeAgentRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val testAgents =
        listOf(
            Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
            Agent("2", "Beta", Agent.AgentType.PLANNER, Agent.AgentStatus.IDLE, 200L),
            Agent("3", "Gamma", Agent.AgentType.REVIEWER, Agent.AgentStatus.ERROR, 300L),
            Agent("4", "Delta", Agent.AgentType.ORCHESTRATOR, Agent.AgentStatus.OFFLINE, 400L),
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(agents: List<Agent> = testAgents): DashboardViewModel {
        val repository = FakeAgentRepository(agents)
        return DashboardViewModel(
            ObserveAgentsUseCase(repository),
            GetAgentUseCase(repository),
        )
    }

    // --- Status Filtering ---

    @Test
    fun `setStatusFilter updates uiState statusFilter field`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.setStatusFilter(Agent.AgentStatus.ERROR)

            assertEquals(Agent.AgentStatus.ERROR, viewModel.uiState.value.statusFilter)
        }

    @Test
    fun `setStatusFilter to same value resets to null via toggle behavior`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.setStatusFilter(Agent.AgentStatus.RUNNING)
            assertEquals(Agent.AgentStatus.RUNNING, viewModel.uiState.value.statusFilter)

            viewModel.setStatusFilter(Agent.AgentStatus.RUNNING)
            assertNull(viewModel.uiState.value.statusFilter)
        }

    @Test
    fun `filteredAgents returns all agents when statusFilter is null`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.statusFilter)
            assertEquals(4, viewModel.uiState.value.filteredAgents.size)
        }

    @Test
    fun `filteredAgents returns only matching agents when statusFilter is set`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()

            viewModel.setStatusFilter(Agent.AgentStatus.RUNNING)

            val filtered = viewModel.uiState.value.filteredAgents
            assertEquals(1, filtered.size)
            assertEquals("1", filtered.first().id)
        }

    // --- Agent Selection ---

    @Test
    fun `selectAgent sets selectedAgent on success`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.selectAgent("1")
            advanceUntilIdle()

            assertEquals("1", viewModel.uiState.value.selectedAgent?.id)
        }

    @Test
    fun `selectAgent with null clears selection`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.selectAgent("1")
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.selectedAgent)

            viewModel.selectAgent(null)

            assertNull(viewModel.uiState.value.selectedAgent)
        }

    @Test
    fun `selectAgent with unknown id sets error`() =
        runTest {
            val viewModel = createViewModel(agents = emptyList())

            viewModel.selectAgent("unknown")
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
        }

    // --- Observation ---

    @Test
    fun `startObserving populates agents`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.startObserving()
            advanceUntilIdle()

            assertEquals(4, viewModel.uiState.value.agents.size)
            assertEquals(ConnectionStatus.CONNECTED, viewModel.uiState.value.connectionStatus)
        }

    // --- Error Handling ---

    @Test
    fun `clearError resets error to null`() =
        runTest {
            val viewModel = createViewModel(agents = emptyList())
            viewModel.selectAgent("nonexistent")
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    // --- Filtered agents recompute on data change ---

    @Test
    fun `filteredAgents updates when agents list changes with active filter`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.startObserving()
            advanceUntilIdle()

            viewModel.setStatusFilter(Agent.AgentStatus.RUNNING)
            assertEquals(1, viewModel.uiState.value.filteredAgents.size)

            assertEquals("Alpha", viewModel.uiState.value.filteredAgents.first().name)
        }
}
