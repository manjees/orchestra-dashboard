package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.usecase.GetCheckpointsUseCase
import com.orchestradashboard.shared.domain.usecase.RetryCheckpointUseCase
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
class ProjectExplorerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private val failedCheckpoints =
        listOf(
            Checkpoint("cp-1", "pipe-1", "build", CheckpointStatus.FAILED, 1000L),
            Checkpoint("cp-2", "pipe-2", "test", CheckpointStatus.FAILED, 2000L),
        )

    private lateinit var repository: FakeCheckpointRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCheckpointRepository()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProjectExplorerViewModel =
        ProjectExplorerViewModel(
            getCheckpointsUseCase = GetCheckpointsUseCase(repository),
            retryCheckpointUseCase = RetryCheckpointUseCase(repository),
        )

    @Test
    fun `initial state has empty checkpoints and isLoading false and no error`() =
        runTest {
            val viewModel = createViewModel()
            val state = viewModel.uiState.value

            assertTrue(state.checkpoints.isEmpty())
            assertEquals(false, state.isLoading)
            assertNull(state.error)
            assertNull(state.retryingCheckpointId)
            assertNull(state.retryResult)
        }

    @Test
    fun `loadCheckpoints sets isLoading then populates checkpoints list`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.success(failedCheckpoints)
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(false, state.isLoading)
            assertEquals(2, state.checkpoints.size)
            assertEquals("cp-1", state.checkpoints[0].id)
            assertEquals("cp-2", state.checkpoints[1].id)
        }

    @Test
    fun `loadCheckpoints sets error on failure and clears isLoading`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.failure(RuntimeException("Network error"))
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(false, state.isLoading)
            assertEquals("Network error", state.error)
            assertTrue(state.checkpoints.isEmpty())
        }

    @Test
    fun `retryCheckpoint sets retryingCheckpointId for that checkpoint`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.success(failedCheckpoints)
            val retriedCheckpoint = failedCheckpoints[0].copy(status = CheckpointStatus.RUNNING)
            repository.retryCheckpointResult = Result.success(retriedCheckpoint)
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()

            viewModel.retryCheckpoint("cp-1")
            advanceUntilIdle()

            assertEquals("cp-1", repository.lastRetriedCheckpointId)
        }

    @Test
    fun `retryCheckpoint success triggers reload of checkpoints`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.success(failedCheckpoints)
            val retriedCheckpoint = failedCheckpoints[0].copy(status = CheckpointStatus.RUNNING)
            repository.retryCheckpointResult = Result.success(retriedCheckpoint)
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()

            // After retry, the reload should re-fetch checkpoints
            viewModel.retryCheckpoint("cp-1")
            advanceUntilIdle()

            // getFailedCheckpoints should be called twice (initial load + post-retry reload)
            assertEquals(2, repository.getFailedCheckpointsCallCount)
        }

    @Test
    fun `retryCheckpoint failure sets retryResult with Failure`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.success(failedCheckpoints)
            repository.retryCheckpointResult = Result.failure(RuntimeException("Retry failed"))
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()

            viewModel.retryCheckpoint("cp-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.retryingCheckpointId)
            assertTrue(state.retryResult is RetryResult.Failure)
            assertEquals("cp-1", (state.retryResult as RetryResult.Failure).checkpointId)
            assertEquals("Retry failed", (state.retryResult as RetryResult.Failure).message)
        }

    @Test
    fun `retryCheckpoint success sets retryResult with Success`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.success(failedCheckpoints)
            val retriedCheckpoint = failedCheckpoints[0].copy(status = CheckpointStatus.RUNNING)
            repository.retryCheckpointResult = Result.success(retriedCheckpoint)
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()

            viewModel.retryCheckpoint("cp-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.retryingCheckpointId)
            assertTrue(state.retryResult is RetryResult.Success)
            assertEquals("cp-1", (state.retryResult as RetryResult.Success).checkpointId)
        }

    @Test
    fun `clearError resets error to null`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.failure(RuntimeException("Error"))
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()
            assertEquals("Error", viewModel.uiState.value.error)

            viewModel.clearError()
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `clearRetryResult resets retryResult to null`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.success(failedCheckpoints)
            repository.retryCheckpointResult = Result.failure(RuntimeException("Retry failed"))
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()

            viewModel.retryCheckpoint("cp-1")
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.retryResult is RetryResult.Failure)

            viewModel.clearRetryResult()
            assertNull(viewModel.uiState.value.retryResult)
        }

    @Test
    fun `onCleared cancels all coroutines`() =
        runTest {
            repository.getFailedCheckpointsResult = Result.success(failedCheckpoints)
            val viewModel = createViewModel()

            viewModel.loadCheckpoints()
            advanceUntilIdle()
            assertEquals(2, viewModel.uiState.value.checkpoints.size)

            viewModel.onCleared()

            // After clear, change repository result and try to load again
            repository.getFailedCheckpointsResult = Result.success(emptyList())
            viewModel.loadCheckpoints()
            advanceUntilIdle()

            // State should not have changed since scope is cancelled
            assertEquals(2, viewModel.uiState.value.checkpoints.size)
        }
}
