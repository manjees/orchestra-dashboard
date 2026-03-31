package com.orchestradashboard.shared.ui.dashboardhome

import com.orchestradashboard.shared.domain.model.ActivePipeline
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.SystemStatus
import com.orchestradashboard.shared.domain.model.ThermalPressure
import com.orchestradashboard.shared.domain.repository.SystemEventData
import com.orchestradashboard.shared.domain.usecase.GetActivePipelinesUseCase
import com.orchestradashboard.shared.domain.usecase.GetPipelineHistoryUseCase
import com.orchestradashboard.shared.domain.usecase.GetSystemStatusUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveSystemEventsUseCase
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
class DashboardHomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSystemRepository
    private lateinit var viewModel: DashboardHomeViewModel

    private val testStatus = SystemStatus(72.0, 45.0, 58.0, ThermalPressure.NOMINAL)

    private val testPipelines =
        listOf(
            ActivePipeline("p1", "project-a", 1, "Fix bug", "building", 120.0, "RUNNING"),
        )

    private val testResults =
        listOf(
            PipelineResult("h1", "project-a", 1, PipelineRunStatus.PASSED, 300.0, "2024-01-01T01:00:00Z"),
            PipelineResult("h2", "project-b", 2, PipelineRunStatus.FAILED, 150.0, null),
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSystemRepository()
        viewModel =
            DashboardHomeViewModel(
                getSystemStatusUseCase = GetSystemStatusUseCase(repository),
                getActivePipelinesUseCase = GetActivePipelinesUseCase(repository),
                getPipelineHistoryUseCase = GetPipelineHistoryUseCase(repository),
                observeSystemEventsUseCase = ObserveSystemEventsUseCase(repository),
            )
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty lists and isLoading false`() {
        val state = viewModel.uiState.value
        assertNull(state.systemStatus)
        assertTrue(state.activePipelines.isEmpty())
        assertTrue(state.recentResults.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadInitialData sets isLoading true then false`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)

            viewModel.loadInitialData()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `loadInitialData sets systemStatus on success`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)

            viewModel.loadInitialData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.systemStatus)
            assertEquals(72.0, state.systemStatus!!.ramPercent)
            assertEquals(45.0, state.systemStatus!!.cpuPercent)
        }

    @Test
    fun `loadInitialData sets activePipelines on success`() =
        runTest {
            repository.activePipelinesResult = Result.success(testPipelines)

            viewModel.loadInitialData()
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.activePipelines.size)
            assertEquals("p1", viewModel.uiState.value.activePipelines[0].id)
        }

    @Test
    fun `loadInitialData sets recentResults on success`() =
        runTest {
            repository.pipelineHistoryResult = Result.success(testResults)

            viewModel.loadInitialData()
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.recentResults.size)
        }

    @Test
    fun `loadInitialData loads status pipelines and history in parallel`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)
            repository.activePipelinesResult = Result.success(testPipelines)
            repository.pipelineHistoryResult = Result.success(testResults)

            viewModel.loadInitialData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.systemStatus)
            assertEquals(1, state.activePipelines.size)
            assertEquals(2, state.recentResults.size)
            assertEquals(1, repository.getSystemStatusCallCount)
            assertEquals(1, repository.getActivePipelinesCallCount)
            assertEquals(1, repository.getPipelineHistoryCallCount)
        }

    @Test
    fun `loadInitialData sets error when status call fails`() =
        runTest {
            repository.systemStatusResult = Result.failure(RuntimeException("status error"))

            viewModel.loadInitialData()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertEquals("status error", viewModel.uiState.value.error)
        }

    @Test
    fun `loadInitialData sets error when pipelines call fails`() =
        runTest {
            repository.activePipelinesResult = Result.failure(RuntimeException("pipeline error"))

            viewModel.loadInitialData()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
        }

    @Test
    fun `loadInitialData loads partial data when one call fails`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)
            repository.activePipelinesResult = Result.failure(RuntimeException("pipeline error"))
            repository.pipelineHistoryResult = Result.success(testResults)

            viewModel.loadInitialData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.systemStatus)
            assertTrue(state.activePipelines.isEmpty())
            assertEquals(2, state.recentResults.size)
            assertNotNull(state.error)
        }

    @Test
    fun `startObserving subscribes to system events flow`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)
            viewModel.loadInitialData()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            assertEquals(ConnectionStatus.CONNECTED, viewModel.uiState.value.connectionStatus)
        }

    @Test
    fun `system event with ram and cpu updates systemStatus`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)
            viewModel.loadInitialData()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                SystemEventData(ramPercent = 85.0, cpuPercent = 70.0, thermal = "moderate"),
            )
            advanceUntilIdle()

            val status = viewModel.uiState.value.systemStatus
            assertNotNull(status)
            assertEquals(85.0, status.ramPercent)
            assertEquals(70.0, status.cpuPercent)
            assertEquals(ThermalPressure.MODERATE, status.thermalPressure)
        }

    @Test
    fun `system event with step update refreshes activePipelines`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)
            repository.activePipelinesResult = Result.success(testPipelines)
            viewModel.loadInitialData()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            assertEquals(1, repository.getActivePipelinesCallCount)

            repository.eventsFlow.emit(SystemEventData(step = "testing"))
            advanceUntilIdle()

            assertEquals(2, repository.getActivePipelinesCallCount)
        }

    @Test
    fun `refresh resets state and calls loadInitialData`() =
        runTest {
            repository.systemStatusResult = Result.success(testStatus)
            repository.activePipelinesResult = Result.success(testPipelines)

            viewModel.loadInitialData()
            advanceUntilIdle()

            repository.systemStatusResult =
                Result.success(
                    testStatus.copy(ramPercent = 90.0),
                )
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(90.0, viewModel.uiState.value.systemStatus?.ramPercent)
        }

    @Test
    fun `empty activePipelines shows hasActivePipelines false`() =
        runTest {
            repository.activePipelinesResult = Result.success(emptyList())

            viewModel.loadInitialData()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.hasActivePipelines)
        }

    @Test
    fun `empty recentResults shows hasRecentResults false`() =
        runTest {
            repository.pipelineHistoryResult = Result.success(emptyList())

            viewModel.loadInitialData()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.hasRecentResults)
        }

    @Test
    fun `clearError sets error to null`() =
        runTest {
            repository.systemStatusResult = Result.failure(RuntimeException("error"))
            viewModel.loadInitialData()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }
}
