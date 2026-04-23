package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.analytics.AnalyticsSummaryDto
import com.orchestradashboard.shared.data.dto.analytics.DurationTrendDto
import com.orchestradashboard.shared.data.dto.analytics.StepFailureDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsMapperTest {
    private val mapper = AnalyticsMapper()

    @Test
    fun `toDomain maps AnalyticsSummaryDto to PipelineAnalytics correctly`() {
        val dto =
            AnalyticsSummaryDto(
                project = "my-project",
                successRate = 0.87,
                avgDurationSec = 145.5,
                totalRuns = 50,
                failedRuns = 6,
            )

        val result = mapper.toDomain(dto)

        assertEquals("my-project", result.project)
        assertEquals(0.87, result.successRate)
        assertEquals(145.5, result.avgDurationSec)
        assertEquals(50, result.totalRuns)
        assertEquals(6, result.failedRuns)
    }

    @Test
    fun `toStepFailureRates maps list of StepFailureDto to list of StepFailureRate`() {
        val dtos =
            listOf(
                StepFailureDto(stepName = "build", totalCount = 20, failedCount = 4, failureRate = 0.2),
                StepFailureDto(stepName = "test", totalCount = 20, failedCount = 2, failureRate = 0.1),
            )

        val result = mapper.toStepFailureRates(dtos)

        assertEquals(2, result.size)
        assertEquals("build", result[0].stepName)
        assertEquals(20, result[0].totalCount)
        assertEquals(4, result[0].failedCount)
        assertEquals(0.2, result[0].failureRate)
        assertEquals("test", result[1].stepName)
        assertEquals(0.1, result[1].failureRate)
    }

    @Test
    fun `toStepFailureRates with empty list returns empty list`() {
        val result = mapper.toStepFailureRates(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDurationTrends maps list of DurationTrendDto to list of DurationTrend`() {
        val dtos =
            listOf(
                DurationTrendDto(date = "2024-01-01", avgDurationSec = 120.0, runCount = 5),
                DurationTrendDto(date = "2024-01-02", avgDurationSec = 95.5, runCount = 3),
            )

        val result = mapper.toDurationTrends(dtos)

        assertEquals(2, result.size)
        assertEquals("2024-01-01", result[0].date)
        assertEquals(120.0, result[0].avgDurationSec)
        assertEquals(5, result[0].runCount)
        assertEquals("2024-01-02", result[1].date)
        assertEquals(95.5, result[1].avgDurationSec)
        assertEquals(3, result[1].runCount)
    }

    @Test
    fun `toDurationTrends with empty list returns empty list`() {
        val result = mapper.toDurationTrends(emptyList())
        assertTrue(result.isEmpty())
    }
}
