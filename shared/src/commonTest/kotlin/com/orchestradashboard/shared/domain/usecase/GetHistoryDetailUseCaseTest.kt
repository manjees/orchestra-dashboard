package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.ui.history.FakeHistoryRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetHistoryDetailUseCaseTest {
    private val repository = FakeHistoryRepository()
    private val useCase = GetHistoryDetailUseCase(repository)

    private val sampleDetail =
        HistoryDetail(
            id = "h-1",
            projectName = "proj",
            issueNum = 42,
            issueTitle = "Fix bug",
            mode = "solve",
            status = PipelineRunStatus.PASSED,
            startedAt = 1700000000L,
            completedAt = 1700003600L,
            elapsedTotalSec = 3600.0,
            prUrl = "https://github.com/org/repo/pull/1",
            steps = emptyList(),
        )

    @Test
    fun `invoke with valid id returns HistoryDetail`() =
        runTest {
            repository.historyDetailResult = Result.success(sampleDetail)

            val result = useCase("h-1")

            assertTrue(result.isSuccess)
            assertEquals("h-1", result.getOrThrow().id)
            assertEquals("h-1", repository.lastRequestedId)
        }

    @Test
    fun `invoke with invalid id returns failure`() =
        runTest {
            repository.historyDetailResult = Result.failure(RuntimeException("not found"))

            val result = useCase("missing")

            assertTrue(result.isFailure)
        }
}
