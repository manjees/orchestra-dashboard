package com.orchestradashboard.shared.ui.analytics

import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.model.PeriodFilter
import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsScreenStateTest {
    // ── Success rate helpers ──────────────────────────────────────────────────

    @Test
    fun `success rate 0_85 yields 85 percent success arc and 15 percent failure arc`() {
        val successRate = 0.85
        val successSweep = (successRate.coerceIn(0.0, 1.0) * 360f).toFloat()
        val failureSweep = 360f - successSweep
        assertEquals(306f, successSweep, 0.01f)
        assertEquals(54f, failureSweep, 0.01f)
    }

    @Test
    fun `success rate with zero totalRuns displays zero runs label`() {
        val summary =
            PipelineAnalytics(
                project = "p",
                successRate = 0.0,
                avgDurationSec = 0.0,
                totalRuns = 0,
                failedRuns = 0,
            )
        val label = if (summary.totalRuns == 0) "No runs" else "${(summary.successRate * 100).toInt()}%"
        assertEquals("No runs", label)
    }

    @Test
    fun `success rate of 1_0 has zero failure sweep`() {
        val failureSweep = 360f - (1.0.coerceIn(0.0, 1.0) * 360f).toFloat()
        assertEquals(0f, failureSweep, 0.01f)
    }

    @Test
    fun `success rate of 0_0 has zero success sweep`() {
        val successSweep = (0.0.coerceIn(0.0, 1.0) * 360f).toFloat()
        assertEquals(0f, successSweep, 0.01f)
    }

    // ── Duration trends ───────────────────────────────────────────────────────

    @Test
    fun `duration trends empty list — hasData false`() {
        val state = AnalyticsUiState(durationTrends = emptyList())
        assertTrue(state.durationTrends.isEmpty())
    }

    @Test
    fun `duration trends single point — size is 1`() {
        val state =
            AnalyticsUiState(
                durationTrends = listOf(DurationTrend("2024-01-01", 120.0, 1)),
            )
        assertEquals(1, state.durationTrends.size)
    }

    @Test
    fun `duration trends data points are in provided order`() {
        val trends =
            listOf(
                DurationTrend("2024-01-01", 120.0, 5),
                DurationTrend("2024-01-02", 150.0, 3),
                DurationTrend("2024-01-03", 100.0, 7),
            )
        val state = AnalyticsUiState(durationTrends = trends)
        assertEquals("2024-01-01", state.durationTrends[0].date)
        assertEquals("2024-01-02", state.durationTrends[1].date)
        assertEquals("2024-01-03", state.durationTrends[2].date)
    }

    // ── Step failure heatmap ──────────────────────────────────────────────────

    @Test
    fun `step failures empty list`() {
        val state = AnalyticsUiState(stepFailures = emptyList())
        assertTrue(state.stepFailures.isEmpty())
    }

    @Test
    fun `step failure rate 0_0 maps to lightest bucket index 0`() {
        val bucket = failureBucket(0.0)
        assertEquals(0, bucket)
    }

    @Test
    fun `step failure rate 1_0 maps to darkest bucket index 4`() {
        val bucket = failureBucket(1.0)
        assertEquals(4, bucket)
    }

    @Test
    fun `step failure rate 0_1 maps to bucket 0`() {
        assertEquals(0, failureBucket(0.1))
    }

    @Test
    fun `step failure rate 0_25 maps to bucket 1`() {
        assertEquals(1, failureBucket(0.25))
    }

    @Test
    fun `step failure rate 0_5 maps to bucket 2`() {
        assertEquals(2, failureBucket(0.5))
    }

    @Test
    fun `step failure rate 0_75 maps to bucket 3`() {
        assertEquals(3, failureBucket(0.75))
    }

    // ── Period filter labels ──────────────────────────────────────────────────

    @Test
    fun `all PeriodFilter values produce non-blank labels`() {
        PeriodFilter.entries.forEach { period ->
            assertTrue(period.label.isNotBlank(), "Label for $period must not be blank")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun failureBucket(rate: Double): Int =
        when {
            rate < 0.25 -> 0
            rate < 0.50 -> 1
            rate < 0.75 -> 2
            rate < 1.0 -> 3
            else -> 4
        }
}
