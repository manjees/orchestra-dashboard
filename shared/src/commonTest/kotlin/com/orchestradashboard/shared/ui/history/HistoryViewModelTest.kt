package com.orchestradashboard.shared.ui.history

import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.usecase.GetHistoryDetailUseCase
import com.orchestradashboard.shared.domain.usecase.GetPagedHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeHistoryRepository
    private lateinit var viewModel: HistoryViewModel

    private val sampleResult =
        PipelineResult("h1", "proj-a", 1, PipelineRunStatus.PASSED, 300.0, "2024-01-01T01:00:00Z")
    private val secondResult =
        PipelineResult("h2", "proj-b", 2, PipelineRunStatus.FAILED, 120.0, null)

    private val sampleDetail =
        HistoryDetail(
            id = "h1",
            projectName = "proj-a",
            issueNum = 1,
            issueTitle = "Fix bug",
            mode = "solve",
            status = PipelineRunStatus.PASSED,
            startedAt = 1700000000L,
            completedAt = 1700003600L,
            elapsedTotalSec = 3600.0,
            prUrl = null,
            steps = emptyList(),
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHistoryRepository()
        viewModel =
            HistoryViewModel(
                getPagedHistoryUseCase = GetPagedHistoryUseCase(repository),
                getHistoryDetailUseCase = GetHistoryDetailUseCase(repository),
            )
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty items and isLoading false`() {
        val state = viewModel.uiState.value
        assertTrue(state.historyItems.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.hasResults)
        assertFalse(state.isFiltered)
        assertFalse(state.showDetail)
    }

    @Test
    fun `loadInitialData populates historyItems on success`() =
        runTest {
            repository.pagedHistoryResult =
                Result.success(PagedResult(listOf(sampleResult, secondResult), 0, 20, 2L, 1))

            viewModel.loadInitialData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.historyItems.size)
            assertFalse(state.isLoading)
            assertEquals(0, state.currentPage)
            assertEquals(1, state.totalPages)
            assertFalse(state.hasNextPage)
        }

    @Test
    fun `loadInitialData sets error on failure`() =
        runTest {
            repository.pagedHistoryResult = Result.failure(RuntimeException("network error"))

            viewModel.loadInitialData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("network error", state.error)
            assertTrue(state.historyItems.isEmpty())
        }

    @Test
    fun `applyFilter with project updates filter and reloads from page 0`() =
        runTest {
            repository.pagedHistoryResult =
                Result.success(PagedResult(listOf(sampleResult), 0, 20, 1L, 1))

            viewModel.applyFilter(HistoryFilter(project = "proj-a"))
            advanceUntilIdle()

            assertEquals("proj-a", viewModel.uiState.value.filter.project)
            assertEquals(0, repository.lastPage)
            assertEquals("proj-a", repository.lastFilter?.project)
        }

    @Test
    fun `applyFilter with status updates filter and reloads from page 0`() =
        runTest {
            viewModel.applyFilter(HistoryFilter(status = PipelineRunStatus.FAILED))
            advanceUntilIdle()

            assertEquals(PipelineRunStatus.FAILED, viewModel.uiState.value.filter.status)
            assertEquals(0, repository.lastPage)
        }

    @Test
    fun `updateSearchQuery updates state immediately`() {
        viewModel.updateSearchQuery("bug")

        assertEquals("bug", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `updateSearchQuery applies keyword to filter after debounce`() =
        runTest {
            viewModel.updateSearchQuery("bug")
            advanceTimeBy(SEARCH_DEBOUNCE_WINDOW_MS)
            advanceUntilIdle()

            assertEquals("bug", viewModel.uiState.value.filter.keyword)
            assertEquals("bug", repository.lastFilter?.keyword)
        }

    @Test
    fun `updateSearchQuery with empty string clears keyword filter`() =
        runTest {
            viewModel.updateSearchQuery("bug")
            advanceTimeBy(SEARCH_DEBOUNCE_WINDOW_MS)
            advanceUntilIdle()

            viewModel.updateSearchQuery("")
            advanceTimeBy(SEARCH_DEBOUNCE_WINDOW_MS)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.filter.keyword)
        }

    @Test
    fun `loadNextPage increments page and appends results`() =
        runTest {
            repository.pagedHistoryResult =
                Result.success(PagedResult(listOf(sampleResult), 0, 20, 2L, 2))
            viewModel.loadInitialData()
            advanceUntilIdle()

            repository.pagedHistoryResult =
                Result.success(PagedResult(listOf(secondResult), 1, 20, 2L, 2))
            viewModel.loadNextPage()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.historyItems.size)
            assertEquals("h1", state.historyItems[0].id)
            assertEquals("h2", state.historyItems[1].id)
            assertEquals(1, state.currentPage)
        }

    @Test
    fun `loadNextPage does nothing when hasNextPage is false`() =
        runTest {
            repository.pagedHistoryResult =
                Result.success(PagedResult(listOf(sampleResult), 0, 20, 1L, 1))
            viewModel.loadInitialData()
            advanceUntilIdle()

            val callsBeforeLoadNext = repository.getPagedHistoryCallCount
            viewModel.loadNextPage()
            advanceUntilIdle()

            assertEquals(callsBeforeLoadNext, repository.getPagedHistoryCallCount)
        }

    @Test
    fun `selectHistory sets selectedHistoryId and loads detail`() =
        runTest {
            repository.historyDetailResult = Result.success(sampleDetail)

            viewModel.selectHistory("h1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("h1", state.selectedHistoryId)
            assertNotNull(state.historyDetail)
            assertEquals("h1", state.historyDetail?.id)
            assertFalse(state.isLoadingDetail)
            assertTrue(state.showDetail)
        }

    @Test
    fun `selectHistory sets detailError on failure`() =
        runTest {
            repository.historyDetailResult = Result.failure(RuntimeException("not found"))

            viewModel.selectHistory("missing")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("missing", state.selectedHistoryId)
            assertEquals("not found", state.detailError)
            assertNull(state.historyDetail)
            assertFalse(state.isLoadingDetail)
        }

    @Test
    fun `clearSelection resets selectedHistoryId and detail`() =
        runTest {
            repository.historyDetailResult = Result.success(sampleDetail)
            viewModel.selectHistory("h1")
            advanceUntilIdle()

            viewModel.clearSelection()

            val state = viewModel.uiState.value
            assertNull(state.selectedHistoryId)
            assertNull(state.historyDetail)
            assertNull(state.detailError)
            assertFalse(state.isLoadingDetail)
        }

    @Test
    fun `refresh reloads from page 0`() =
        runTest {
            repository.pagedHistoryResult =
                Result.success(PagedResult(listOf(sampleResult), 0, 20, 1L, 1))
            viewModel.loadInitialData()
            advanceUntilIdle()

            val callsBeforeRefresh = repository.getPagedHistoryCallCount
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(callsBeforeRefresh + 1, repository.getPagedHistoryCallCount)
            assertEquals(0, repository.lastPage)
        }

    @Test
    fun `clearError resets error to null`() =
        runTest {
            repository.pagedHistoryResult = Result.failure(RuntimeException("error"))
            viewModel.loadInitialData()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `selectTimeRange updates filter and reloads`() =
        runTest {
            viewModel.selectTimeRange(com.orchestradashboard.shared.domain.model.TimeRange.Last7Days)
            advanceUntilIdle()

            assertEquals(
                com.orchestradashboard.shared.domain.model.TimeRange.Last7Days,
                viewModel.uiState.value.filter.timeRange,
            )
            assertEquals(0, repository.lastPage)
        }

    companion object {
        private const val SEARCH_DEBOUNCE_WINDOW_MS = 350L
    }
}
