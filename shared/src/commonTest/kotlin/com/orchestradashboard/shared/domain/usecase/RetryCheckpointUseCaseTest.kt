package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.repository.CheckpointRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetryCheckpointUseCaseTest {
    private val fakeRepository = FakeRetryCheckpointRepository()
    private val useCase = RetryCheckpointUseCase(fakeRepository)

    @Test
    fun `invoke calls repository retryCheckpoint and returns success result`() =
        runTest {
            val retriedCheckpoint =
                Checkpoint("cp-1", "pipeline-1", Instant.parse("2024-01-01T00:00:00Z"), "build", CheckpointStatus.RUNNING)
            fakeRepository.retryCheckpointResult = Result.success(retriedCheckpoint)

            val result = useCase("cp-1")

            assertTrue(result.isSuccess)
            assertEquals(retriedCheckpoint, result.getOrNull())
            assertEquals(1, fakeRepository.retryCheckpointCallCount)
        }

    @Test
    fun `invoke returns failure when repository fails`() =
        runTest {
            val error = RuntimeException("Server error")
            fakeRepository.retryCheckpointResult = Result.failure(error)

            val result = useCase("cp-1")

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}

private class FakeRetryCheckpointRepository : CheckpointRepository {
    var retryCheckpointResult: Result<Checkpoint> = Result.failure(NotImplementedError())
    var retryCheckpointCallCount = 0

    override suspend fun getFailedCheckpoints(): Result<List<Checkpoint>> = Result.success(emptyList())

    override suspend fun retryCheckpoint(checkpointId: String): Result<Checkpoint> {
        retryCheckpointCallCount++
        return retryCheckpointResult
    }
}
