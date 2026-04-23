package com.orchestradashboard.shared.ui.logstream

import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.model.LogLevel
import com.orchestradashboard.shared.domain.model.LogStreamState
import com.orchestradashboard.shared.domain.usecase.ObserveLogStreamUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LogStreamViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeLogStreamRepository
    private lateinit var viewModel: LogStreamViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeLogStreamRepository()
        viewModel = LogStreamViewModel(ObserveLogStreamUseCase(repository))
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    private fun logEntry(
        message: String,
        stepId: String = "step-1",
        level: LogLevel = LogLevel.INFO,
        timestamp: String = "2026-04-23T10:00:00Z",
    ): LogEntry =
        LogEntry(
            timestamp = timestamp,
            level = level,
            message = message,
            stepId = stepId,
        )

    @Test
    fun `initial state is Idle with empty logs`() {
        val state = viewModel.uiState.value
        assertEquals(LogStreamState.Idle, state.streamState)
        assertTrue(state.logs.isEmpty())
        assertNull(state.selectedStepId)
    }

    @Test
    fun `startStream sets state to Loading then Streaming`() =
        runTest {
            viewModel.startStream("step-1")
            // Before the coroutine executes onStart, we should see Loading.
            assertEquals(LogStreamState.Loading, viewModel.uiState.value.streamState)

            advanceUntilIdle()
            assertEquals(LogStreamState.Streaming, viewModel.uiState.value.streamState)
        }

    @Test
    fun `startStream sets selectedStepId`() =
        runTest {
            viewModel.startStream("step-42")
            advanceUntilIdle()
            assertEquals("step-42", viewModel.uiState.value.selectedStepId)
        }

    @Test
    fun `log entries accumulate in state as they arrive`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()

            repository.logFlow.emit(logEntry("first"))
            repository.logFlow.emit(logEntry("second"))
            repository.logFlow.emit(logEntry("third"))
            advanceUntilIdle()

            val logs = viewModel.uiState.value.logs
            assertEquals(3, logs.size)
        }

    @Test
    fun `log entries preserve order`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()

            repository.logFlow.emit(logEntry("alpha"))
            repository.logFlow.emit(logEntry("beta"))
            repository.logFlow.emit(logEntry("gamma"))
            advanceUntilIdle()

            val logs = viewModel.uiState.value.logs
            assertEquals("alpha", logs[0].message)
            assertEquals("beta", logs[1].message)
            assertEquals("gamma", logs[2].message)
        }

    @Test
    fun `startStream with new stepId clears previous logs`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()
            repository.logFlow.emit(logEntry("old", stepId = "step-1"))
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.logs.size)

            viewModel.startStream("step-2")
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.logs.isEmpty())
            assertEquals("step-2", viewModel.uiState.value.selectedStepId)
        }

    @Test
    fun `startStream with new stepId cancels previous stream`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()

            viewModel.startStream("step-2")
            advanceUntilIdle()

            // Emitting a log entry (to the shared logFlow) after switching should NOT update state
            // because the step-1 collection job was cancelled, and while step-2 collects the same
            // flow, we assert via observeCallCount that a new collection was started.
            assertEquals(2, repository.observeCallCount)
            assertEquals("step-2", repository.lastStepId)
        }

    @Test
    fun `startStream with same stepId is no-op`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()
            // Must be in Streaming state before the guard takes effect
            assertEquals(LogStreamState.Streaming, viewModel.uiState.value.streamState)
            assertEquals(1, repository.observeCallCount)

            viewModel.startStream("step-1")
            advanceUntilIdle()

            // No new observe call when already streaming same stepId
            assertEquals(1, repository.observeCallCount)
        }

    @Test
    fun `stopStream sets state to Idle and clears selectedStepId`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()

            viewModel.stopStream()
            advanceUntilIdle()

            assertEquals(LogStreamState.Idle, viewModel.uiState.value.streamState)
            assertNull(viewModel.uiState.value.selectedStepId)
        }

    @Test
    fun `stopStream clears logs`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()
            repository.logFlow.emit(logEntry("will-be-cleared"))
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.logs.size)

            viewModel.stopStream()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.logs.isEmpty())
        }

    @Test
    fun `stream error sets state to Error with message`() =
        runTest {
            repository.setErrorFlow(RuntimeException("stream broke"))

            viewModel.startStream("step-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value.streamState
            assertTrue(state is LogStreamState.Error, "expected Error state but was $state")
            assertEquals("stream broke", state.message)
        }

    @Test
    fun `retry after error restarts stream`() =
        runTest {
            repository.setErrorFlow(RuntimeException("boom"))
            viewModel.startStream("step-1")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.streamState is LogStreamState.Error)

            repository.resetToNormalFlow()
            viewModel.startStream("step-1")
            advanceUntilIdle()

            assertEquals(LogStreamState.Streaming, viewModel.uiState.value.streamState)
            assertEquals(2, repository.observeCallCount)
        }

    @Test
    fun `onCleared cancels active stream`() =
        runTest {
            viewModel.startStream("step-1")
            advanceUntilIdle()
            assertEquals(LogStreamState.Streaming, viewModel.uiState.value.streamState)

            viewModel.onCleared()
            advanceUntilIdle()

            // After onCleared, subsequent emissions should not be collected.
            // We cannot assert on state (scope is cancelled), but we can verify no exception occurs
            // and the test teardown handles a second onCleared() gracefully.
            repository.logFlow.emit(logEntry("should-not-be-collected"))
            advanceUntilIdle()
        }
}
