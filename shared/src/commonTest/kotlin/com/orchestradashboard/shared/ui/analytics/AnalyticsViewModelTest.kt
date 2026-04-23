package com.orchestradashboard.shared.ui.analytics

import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.model.PeriodFilter
import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import com.orchestradashboard.shared.domain.model.StepFailureRate
import com.orchestradashboard.shared.domain.usecase.GetDurationTrendsUseCase
import com.orchestradashboard.shared.domain.usecase.GetPipelineAnalyticsUseCase
import com.orchestradashboard.shared.domain.usecase.GetStepFailureRatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAnalyticsRepository
    private lateinit var viewModel: AnalyticsViewModel

    private val fixedNow = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val fakeClock = FixedClock(fixedNow)

    private val testSummary =
        PipelineAnalytics(
            project = "test-project",
            successRate = 0.85,
            avgDurationSec = 180.0,
            totalRuns = 40,
            failedRuns = 6,
        )

    private val testTrends =
        listOf(
            DurationTrend(date = "2024-01-01", avgDurationSec = 120.0, runCount = 5),
            DurationTrend(date = "2024-01-02", avgDurationSec = 150.0, runCount = 3),
        )

    private val testFailures =
        listOf(
            StepFailureRate(stepName = "build", totalCount = 20, failedCount = 3, failureRate = 0.15),
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAnalyticsRepository()
        viewModel =
            AnalyticsViewModel(
                getPipelineAnalyticsUseCase = GetPipelineAnalyticsUseCase(repository),
                getDurationTrendsUseCase = GetDurationTrendsUseCase(repository),
                getStepFailureRatesUseCase = GetStepFailureRatesUseCase(repository),
                project = "test-project",
                clock = fakeClock,
            )
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has null data, isLoading false, period ALL, no error`() {
        val state = viewModel.uiState.value
        assertNull(state.summary)
        assertTrue(state.durationTrends.isEmpty())
        assertTrue(state.stepFailures.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(PeriodFilter.ALL, state.selectedPeriod)
        assertNull(state.error)
    }

    @Test
    fun `loadData sets isLoading true then false on completion`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.loadData()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `loadData populates summary on success`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.loadData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.summary)
            assertEquals("test-project", state.summary!!.project)
            assertEquals(0.85, state.summary!!.successRate)
            assertEquals(40, state.summary!!.totalRuns)
        }

    @Test
    fun `loadData populates durationTrends on success`() =
        runTest {
            repository.durationTrendsResult = Result.success(testTrends)

            viewModel.loadData()
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.durationTrends.size)
            assertEquals("2024-01-01", viewModel.uiState.value.durationTrends[0].date)
        }

    @Test
    fun `loadData populates stepFailures on success`() =
        runTest {
            repository.stepFailuresResult = Result.success(testFailures)

            viewModel.loadData()
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.stepFailures.size)
            assertEquals("build", viewModel.uiState.value.stepFailures[0].stepName)
        }

    @Test
    fun `loadData loads all 3 data sources in parallel`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)
            repository.durationTrendsResult = Result.success(testTrends)
            repository.stepFailuresResult = Result.success(testFailures)

            viewModel.loadData()
            advanceUntilIdle()

            assertEquals(1, repository.getSummaryCallCount)
            assertEquals(1, repository.getDurationTrendsCallCount)
            assertEquals(1, repository.getStepFailuresCallCount)
        }

    @Test
    fun `loadData sets error when summary call fails`() =
        runTest {
            repository.summaryResult = Result.failure(RuntimeException("summary error"))

            viewModel.loadData()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertEquals("summary error", viewModel.uiState.value.error)
        }

    @Test
    fun `loadData loads partial data when one source fails`() =
        runTest {
            repository.summaryResult = Result.failure(RuntimeException("summary error"))
            repository.durationTrendsResult = Result.success(testTrends)
            repository.stepFailuresResult = Result.success(testFailures)

            viewModel.loadData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.summary)
            assertEquals(2, state.durationTrends.size)
            assertEquals(1, state.stepFailures.size)
            assertNotNull(state.error)
        }

    @Test
    fun `selectPeriod updates selectedPeriod in state`() =
        runTest {
            viewModel.selectPeriod(PeriodFilter.WEEK)
            advanceUntilIdle()

            assertEquals(PeriodFilter.WEEK, viewModel.uiState.value.selectedPeriod)
        }

    @Test
    fun `selectPeriod with WEEK reloads data`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.selectPeriod(PeriodFilter.WEEK)
            advanceUntilIdle()

            assertEquals(1, repository.getSummaryCallCount)
            assertEquals(PeriodFilter.WEEK, viewModel.uiState.value.selectedPeriod)
        }

    @Test
    fun `selectPeriod with MONTH reloads data`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.selectPeriod(PeriodFilter.MONTH)
            advanceUntilIdle()

            assertEquals(1, repository.getSummaryCallCount)
            assertEquals(PeriodFilter.MONTH, viewModel.uiState.value.selectedPeriod)
        }

    @Test
    fun `selectPeriod with ALL reloads data with null from`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.selectPeriod(PeriodFilter.ALL)
            advanceUntilIdle()

            assertNull(repository.lastFrom)
            assertEquals(1, repository.getSummaryCallCount)
        }

    @Test
    fun `selectPeriod with WEEK passes exact 7-day-ago timestamp to repository`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.selectPeriod(PeriodFilter.WEEK)
            advanceUntilIdle()

            val expectedFrom = fixedNow.minus(7.days).toEpochMilliseconds()
            assertEquals(expectedFrom, repository.lastFrom)
        }

    @Test
    fun `selectPeriod with MONTH passes exact 30-day-ago timestamp to repository`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.selectPeriod(PeriodFilter.MONTH)
            advanceUntilIdle()

            val expectedFrom = fixedNow.minus(30.days).toEpochMilliseconds()
            assertEquals(expectedFrom, repository.lastFrom)
        }

    @Test
    fun `selectPeriod with WEEK uses day granularity`() =
        runTest {
            viewModel.selectPeriod(PeriodFilter.WEEK)
            advanceUntilIdle()

            assertEquals("day", repository.lastGranularity)
        }

    @Test
    fun `selectPeriod with ALL uses week granularity`() =
        runTest {
            viewModel.selectPeriod(PeriodFilter.ALL)
            advanceUntilIdle()

            assertEquals("week", repository.lastGranularity)
        }

    @Test
    fun `refresh reloads all data`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(1, repository.getSummaryCallCount)
            assertEquals(1, repository.getDurationTrendsCallCount)
            assertEquals(1, repository.getStepFailuresCallCount)
        }

    @Test
    fun `clearError sets error to null`() =
        runTest {
            repository.summaryResult = Result.failure(RuntimeException("some error"))
            viewModel.loadData()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `hasData returns true when summary is non-null`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.loadData()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasData)
        }

    @Test
    fun `hasData returns false when summary is null`() {
        assertFalse(viewModel.uiState.value.hasData)
    }

    @Test
    fun `loadData sets error from trends failure when summary succeeds`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)
            repository.durationTrendsResult = Result.failure(RuntimeException("trends error"))
            repository.stepFailuresResult = Result.success(testFailures)

            viewModel.loadData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("trends error", state.error)
            assertNotNull(state.summary)
            assertEquals("test-project", state.summary!!.project)
        }

    @Test
    fun `selectPeriod does not re-fetch step failures`() =
        runTest {
            repository.summaryResult = Result.success(testSummary)

            viewModel.selectPeriod(PeriodFilter.WEEK)
            advanceUntilIdle()

            assertEquals(0, repository.getStepFailuresCallCount)
        }
}
