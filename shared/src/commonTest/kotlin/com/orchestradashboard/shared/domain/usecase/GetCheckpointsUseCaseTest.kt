package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.repository.CheckpointRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCheckpointsUseCaseTest {
    private val fakeRepository = FakeCheckpointRepository()
    private val useCase = GetCheckpointsUseCase(fakeRepository)

    @Test
    fun `invoke calls repository getFailedCheckpoints and returns success result`() =
        runTest {
            val checkpoints =
                listOf(
                    Checkpoint("cp-1", "pipeline-1", Instant.parse("2024-01-01T00:00:00Z"), "build", CheckpointStatus.FAILED),
                    Checkpoint("cp-2", "pipeline-2", Instant.parse("2024-01-01T01:00:00Z"), "test", CheckpointStatus.FAILED),
                )
            fakeRepository.checkpointsResult = Result.success(checkpoints)

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.size)
            assertEquals(1, fakeRepository.getFailedCheckpointsCallCount)
        }

    @Test
    fun `invoke returns failure when repository fails`() =
        runTest {
            val error = RuntimeException("Network error")
            fakeRepository.checkpointsResult = Result.failure(error)

            val result = useCase()

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}

private class FakeCheckpointRepository : CheckpointRepository {
    var checkpointsResult: Result<List<Checkpoint>> = Result.success(emptyList())
    var getFailedCheckpointsCallCount = 0

    override suspend fun getFailedCheckpoints(): Result<List<Checkpoint>> {
        getFailedCheckpointsCallCount++
        return checkpointsResult
    }

    override suspend fun retryCheckpoint(checkpointId: String): Result<Checkpoint> = Result.failure(NotImplementedError())
}
