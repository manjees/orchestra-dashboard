package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.DurationTrendResponse
import com.orchestradashboard.server.model.PipelineAnalyticsResponse
import com.orchestradashboard.server.model.PipelineHistoryEntity
import com.orchestradashboard.server.model.StepFailureRateResponse
import com.orchestradashboard.server.repository.PipelineHistoryJpaRepository
import com.orchestradashboard.server.repository.PipelineStepHistoryJpaRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset

@Service
class PipelineAnalyticsService(
    private val historyRepository: PipelineHistoryJpaRepository,
    private val stepRepository: PipelineStepHistoryJpaRepository,
) {
    companion object {
        val TERMINAL_STATUSES = setOf("PASSED", "FAILED", "CANCELLED")
        const val DAYS_30 = 30L
        const val DAYS_PER_WEEK = 7L
        private const val RATE_PRECISION = 10000.0
        private const val DURATION_PRECISION = 100.0
    }

    @Cacheable("analyticsSummary")
    fun getSummary(
        project: String,
        from: Long?,
        to: Long?,
    ): PipelineAnalyticsResponse {
        val now = System.currentTimeMillis()
        val startTs = from ?: (now - DAYS_30 * 24 * 3600 * 1000)
        val endTs = to ?: now

        val records = historyRepository.findByProjectNameAndStartedAtBetween(project, startTs, endTs)
        val completed = records.filter { it.status in TERMINAL_STATUSES }
        val passed = completed.count { it.status == "PASSED" }
        val failed = completed.count { it.status == "FAILED" }
        val totalRuns = completed.size
        val successRate = if (totalRuns > 0) passed.toDouble() / totalRuns else 0.0
        val avgDuration = if (completed.isNotEmpty()) completed.map { it.elapsedTotalSec }.average() else 0.0

        return PipelineAnalyticsResponse(
            project = project,
            successRate = Math.round(successRate * RATE_PRECISION) / RATE_PRECISION,
            avgDurationSec = Math.round(avgDuration * DURATION_PRECISION) / DURATION_PRECISION,
            totalRuns = totalRuns,
            failedRuns = failed,
        )
    }

    @Cacheable("stepFailures")
    fun getStepFailureRates(project: String): List<StepFailureRateResponse> {
        val records =
            historyRepository.findByProjectNameAndStartedAtBetween(
                project,
                0L,
                System.currentTimeMillis(),
            )
        val historyIds = records.map { it.id }
        if (historyIds.isEmpty()) return emptyList()

        val steps = stepRepository.findByPipelineHistoryIdIn(historyIds)
        return steps.groupBy { it.stepName }.map { (stepName, entries) ->
            val total = entries.size
            val failedCount = entries.count { it.status == "FAILED" }
            val rate = if (total > 0) failedCount.toDouble() / total else 0.0
            StepFailureRateResponse(
                stepName = stepName,
                totalCount = total,
                failedCount = failedCount,
                failureRate = Math.round(rate * RATE_PRECISION) / RATE_PRECISION,
            )
        }
    }

    @Cacheable("durationTrends")
    fun getDurationTrends(
        project: String,
        granularity: String,
    ): List<DurationTrendResponse> {
        val records =
            historyRepository.findByProjectNameAndStartedAtBetween(
                project,
                0L,
                System.currentTimeMillis(),
            )
        val completed = records.filter { it.status in TERMINAL_STATUSES }
        if (completed.isEmpty()) return emptyList()

        return completed
            .groupBy { entity -> truncateToGranularity(entity, granularity) }
            .map { (date, entries) ->
                DurationTrendResponse(
                    date = date,
                    avgDurationSec = Math.round(entries.map { it.elapsedTotalSec }.average() * DURATION_PRECISION) / DURATION_PRECISION,
                    runCount = entries.size,
                )
            }
            .sortedBy { it.date }
    }

    private fun truncateToGranularity(
        entity: PipelineHistoryEntity,
        granularity: String,
    ): String {
        val instant = Instant.ofEpochMilli(entity.startedAt)
        val localDate = instant.atZone(ZoneOffset.UTC).toLocalDate()
        return when (granularity) {
            "week" -> {
                val weekStart = localDate.minusDays(localDate.dayOfWeek.value.toLong() - 1)
                weekStart.toString()
            }
            else -> localDate.toString()
        }
    }
}
