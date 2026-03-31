package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.repository.CheckpointRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetryCheckpointUseCaseTest {
    private val retriedCheckpoint = Checkpoint("cp-1", "pipe-1", "build", CheckpointStatus.RUNNING, 1000L)

    private val fakeRepository = FakeRetryCheckpointRepository()
    private val useCase = RetryCheckpointUseCase(fakeRepository)

    @Test
    fun `invoke delegates to repository with correct checkpoint ID`() =
        runTest {
            fakeRepository.retryCheckpointResult = Result.success(retriedCheckpoint)

            useCase("cp-1")

            assertEquals("cp-1", fakeRepository.lastRetriedCheckpointId)
            assertEquals(1, fakeRepository.retryCheckpointCallCount)
        }

    @Test
    fun `invoke returns success result from repository`() =
        runTest {
            fakeRepository.retryCheckpointResult = Result.success(retriedCheckpoint)

            val result = useCase("cp-1")

            assertTrue(result.isSuccess)
            assertEquals(retriedCheckpoint, result.getOrThrow())
        }

    @Test
    fun `invoke propagates repository failure`() =
        runTest {
            fakeRepository.retryCheckpointResult = Result.failure(RuntimeException("Server error"))

            val result = useCase("cp-1")

            assertTrue(result.isFailure)
            assertEquals("Server error", result.exceptionOrNull()?.message)
        }
}

private class FakeRetryCheckpointRepository : CheckpointRepository {
    var retryCheckpointResult: Result<Checkpoint> = Result.failure(NotImplementedError())
    var retryCheckpointCallCount = 0
        private set
    var lastRetriedCheckpointId: String? = null
        private set

    override suspend fun getFailedCheckpoints(): Result<List<Checkpoint>> = Result.success(emptyList())

    override suspend fun retryCheckpoint(checkpointId: String): Result<Checkpoint> {
        retryCheckpointCallCount++
        lastRetriedCheckpointId = checkpointId
        return retryCheckpointResult
    }
}
