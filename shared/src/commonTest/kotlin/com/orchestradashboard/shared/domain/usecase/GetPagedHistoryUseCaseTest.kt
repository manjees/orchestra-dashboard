package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.ui.history.FakeHistoryRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPagedHistoryUseCaseTest {
    private val repository = FakeHistoryRepository()
    private val useCase = GetPagedHistoryUseCase(repository)

    private val sampleResult =
        PipelineResult("h1", "proj", 1, PipelineRunStatus.PASSED, 300.0, "2024-01-01T01:00:00Z")

    @Test
    fun `invoke with default filter returns first page`() =
        runTest {
            repository.pagedHistoryResult =
                Result.success(PagedResult(listOf(sampleResult), 0, 20, 1L, 1))

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().agents.size)
            assertEquals(HistoryFilter(), repository.lastFilter)
            assertEquals(0, repository.lastPage)
            assertEquals(20, repository.lastPageSize)
        }

    @Test
    fun `invoke passes project filter to repository`() =
        runTest {
            val filter = HistoryFilter(project = "my-project")

            useCase(filter)

            assertEquals("my-project", repository.lastFilter?.project)
        }

    @Test
    fun `invoke passes status filter to repository`() =
        runTest {
            val filter = HistoryFilter(status = PipelineRunStatus.PASSED)

            useCase(filter)

            assertEquals(PipelineRunStatus.PASSED, repository.lastFilter?.status)
        }

    @Test
    fun `invoke passes keyword query to repository`() =
        runTest {
            val filter = HistoryFilter(keyword = "bug")

            useCase(filter)

            assertEquals("bug", repository.lastFilter?.keyword)
        }

    @Test
    fun `invoke passes timeRange to repository`() =
        runTest {
            val filter = HistoryFilter(timeRange = TimeRange.Last24Hours)

            useCase(filter)

            assertEquals(TimeRange.Last24Hours, repository.lastFilter?.timeRange)
        }

    @Test
    fun `invoke passes custom page and size`() =
        runTest {
            useCase(page = 2, pageSize = 50)

            assertEquals(2, repository.lastPage)
            assertEquals(50, repository.lastPageSize)
        }

    @Test
    fun `invoke returns failure when repository fails`() =
        runTest {
            repository.pagedHistoryResult = Result.failure(RuntimeException("db error"))

            val result = useCase()

            assertTrue(result.isFailure)
        }
}
