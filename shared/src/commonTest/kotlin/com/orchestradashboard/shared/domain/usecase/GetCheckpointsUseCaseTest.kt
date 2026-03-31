package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.repository.CheckpointRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCheckpointsUseCaseTest {
    private val failedCheckpoints =
        listOf(
            Checkpoint("cp-1", "pipe-1", "build", CheckpointStatus.FAILED, 1000L),
            Checkpoint("cp-2", "pipe-2", "test", CheckpointStatus.FAILED, 2000L),
        )

    private val fakeRepository = FakeCheckpointRepository()
    private val useCase = GetCheckpointsUseCase(fakeRepository)

    @Test
    fun `invoke delegates to repository getFailedCheckpoints and returns result`() =
        runTest {
            fakeRepository.getFailedCheckpointsResult = Result.success(failedCheckpoints)

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(failedCheckpoints, result.getOrThrow())
            assertEquals(1, fakeRepository.getFailedCheckpointsCallCount)
        }

    @Test
    fun `invoke propagates repository failure`() =
        runTest {
            fakeRepository.getFailedCheckpointsResult = Result.failure(RuntimeException("Network error"))

            val result = useCase()

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }

    @Test
    fun `invoke returns empty list when no failed checkpoints`() =
        runTest {
            fakeRepository.getFailedCheckpointsResult = Result.success(emptyList())

            val result = useCase()

            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isEmpty())
        }
}

private class FakeCheckpointRepository : CheckpointRepository {
    var getFailedCheckpointsResult: Result<List<Checkpoint>> = Result.success(emptyList())
    var retryCheckpointResult: Result<Checkpoint> = Result.failure(NotImplementedError())

    var getFailedCheckpointsCallCount = 0
        private set

    override suspend fun getFailedCheckpoints(): Result<List<Checkpoint>> {
        getFailedCheckpointsCallCount++
        return getFailedCheckpointsResult
    }

    override suspend fun retryCheckpoint(checkpointId: String): Result<Checkpoint> =
        retryCheckpointResult
}
