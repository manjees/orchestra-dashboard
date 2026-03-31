package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.ActivePipeline
import com.orchestradashboard.shared.ui.dashboardhome.FakeSystemRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetActivePipelinesUseCaseTest {
    private val repository = FakeSystemRepository()
    private val useCase = GetActivePipelinesUseCase(repository)

    @Test
    fun `invoke delegates to repository getActivePipelines`() =
        runTest {
            val pipelines =
                listOf(
                    ActivePipeline("p1", "proj", 1, "Title", "build", 120.0, "RUNNING"),
                )
            repository.activePipelinesResult = Result.success(pipelines)

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
            assertEquals(1, repository.getActivePipelinesCallCount)
        }

    @Test
    fun `invoke returns failure on repository error`() =
        runTest {
            repository.activePipelinesResult = Result.failure(RuntimeException("fail"))

            val result = useCase()

            assertTrue(result.isFailure)
        }
}
