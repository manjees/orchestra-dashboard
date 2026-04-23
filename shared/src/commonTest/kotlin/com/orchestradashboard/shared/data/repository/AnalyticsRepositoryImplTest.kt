package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.analytics.AnalyticsSummaryDto
import com.orchestradashboard.shared.data.dto.analytics.DurationTrendDto
import com.orchestradashboard.shared.data.dto.analytics.StepFailureDto
import com.orchestradashboard.shared.data.mapper.AnalyticsMapper
import com.orchestradashboard.shared.data.network.FakeDashboardApiClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsRepositoryImplTest {
    private val fakeApi = FakeDashboardApiClient()
    private val mapper = AnalyticsMapper()
    private val repository = AnalyticsRepositoryImpl(fakeApi, mapper)

    @Test
    fun `getAnalyticsSummary returns success with mapped domain model`() =
        runTest {
            fakeApi.analyticsSummaryResponse =
                AnalyticsSummaryDto(
                    project = "proj-a",
                    successRate = 0.9,
                    avgDurationSec = 200.0,
                    totalRuns = 100,
                    failedRuns = 10,
                )

            val result = repository.getAnalyticsSummary("proj-a")

            assertTrue(result.isSuccess)
            val model = result.getOrThrow()
            assertEquals("proj-a", model.project)
            assertEquals(0.9, model.successRate)
            assertEquals(200.0, model.avgDurationSec)
            assertEquals(100, model.totalRuns)
            assertEquals(10, model.failedRuns)
        }

    @Test
    fun `getAnalyticsSummary passes from and to parameters to API`() =
        runTest {
            fakeApi.analyticsSummaryResponse =
                AnalyticsSummaryDto(
                    project = "proj-a",
                    successRate = 0.0,
                    avgDurationSec = 0.0,
                    totalRuns = 0,
                    failedRuns = 0,
                )

            repository.getAnalyticsSummary("proj-a", from = 1000L, to = 2000L)

            assertEquals("proj-a", fakeApi.lastAnalyticsProject)
            assertEquals(1000L, fakeApi.lastAnalyticsFrom)
            assertEquals(2000L, fakeApi.lastAnalyticsTo)
        }

    @Test
    fun `getAnalyticsSummary returns failure when API throws`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("network error")

            val result = repository.getAnalyticsSummary("proj-a")

            assertTrue(result.isFailure)
            assertEquals("network error", result.exceptionOrNull()?.message)
        }

    @Test
    fun `getStepFailureRates returns success with mapped list`() =
        runTest {
            fakeApi.stepFailuresResponse =
                listOf(
                    StepFailureDto(stepName = "build", totalCount = 10, failedCount = 2, failureRate = 0.2),
                )

            val result = repository.getStepFailureRates("proj-a")

            assertTrue(result.isSuccess)
            val list = result.getOrThrow()
            assertEquals(1, list.size)
            assertEquals("build", list[0].stepName)
            assertEquals(0.2, list[0].failureRate)
        }

    @Test
    fun `getStepFailureRates returns failure when API throws`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("step failures error")

            val result = repository.getStepFailureRates("proj-a")

            assertTrue(result.isFailure)
            assertEquals("step failures error", result.exceptionOrNull()?.message)
        }

    @Test
    fun `getDurationTrends returns success with mapped list`() =
        runTest {
            fakeApi.durationTrendsResponse =
                listOf(
                    DurationTrendDto(date = "2024-01-01", avgDurationSec = 130.0, runCount = 4),
                )

            val result = repository.getDurationTrends("proj-a", "day")

            assertTrue(result.isSuccess)
            val list = result.getOrThrow()
            assertEquals(1, list.size)
            assertEquals("2024-01-01", list[0].date)
            assertEquals(130.0, list[0].avgDurationSec)
            assertEquals(4, list[0].runCount)
        }

    @Test
    fun `getDurationTrends passes granularity parameter to API`() =
        runTest {
            repository.getDurationTrends("proj-a", "week")

            assertEquals("week", fakeApi.lastDurationGranularity)
        }

    @Test
    fun `getDurationTrends returns failure when API throws`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("trends error")

            val result = repository.getDurationTrends("proj-a", "day")

            assertTrue(result.isFailure)
            assertEquals("trends error", result.exceptionOrNull()?.message)
        }
}
