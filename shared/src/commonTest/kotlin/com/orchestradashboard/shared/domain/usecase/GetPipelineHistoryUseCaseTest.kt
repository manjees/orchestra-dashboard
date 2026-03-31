package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.ui.dashboardhome.FakeSystemRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPipelineHistoryUseCaseTest {
    private val repository = FakeSystemRepository()
    private val useCase = GetPipelineHistoryUseCase(repository)

    @Test
    fun `invoke delegates to repository getPipelineHistory`() =
        runTest {
            val history =
                listOf(
                    PipelineResult("h1", "proj", 1, PipelineRunStatus.PASSED, 300.0, "2024-01-01T01:00:00Z"),
                )
            repository.pipelineHistoryResult = Result.success(history)

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
        }

    @Test
    fun `invoke limits results to 10 items`() =
        runTest {
            val history =
                (1..15).map { i ->
                    PipelineResult("h$i", "proj", i, PipelineRunStatus.PASSED, 300.0, null)
                }
            repository.pipelineHistoryResult = Result.success(history)

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(10, result.getOrThrow().size)
        }

    @Test
    fun `invoke returns failure on repository error`() =
        runTest {
            repository.pipelineHistoryResult = Result.failure(RuntimeException("fail"))

            val result = useCase()

            assertTrue(result.isFailure)
        }
}
