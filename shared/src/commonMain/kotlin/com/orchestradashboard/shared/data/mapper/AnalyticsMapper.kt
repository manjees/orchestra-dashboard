package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.analytics.AnalyticsSummaryDto
import com.orchestradashboard.shared.data.dto.analytics.DurationTrendDto
import com.orchestradashboard.shared.data.dto.analytics.StepFailureDto
import com.orchestradashboard.shared.domain.model.DurationTrend
import com.orchestradashboard.shared.domain.model.PipelineAnalytics
import com.orchestradashboard.shared.domain.model.StepFailureRate

class AnalyticsMapper {
    fun toDomain(dto: AnalyticsSummaryDto): PipelineAnalytics =
        PipelineAnalytics(
            project = dto.project,
            successRate = dto.successRate,
            avgDurationSec = dto.avgDurationSec,
            totalRuns = dto.totalRuns,
            failedRuns = dto.failedRuns,
        )

    fun toStepFailureRates(dtos: List<StepFailureDto>): List<StepFailureRate> =
        dtos.map { dto ->
            StepFailureRate(
                stepName = dto.stepName,
                totalCount = dto.totalCount,
                failedCount = dto.failedCount,
                failureRate = dto.failureRate,
            )
        }

    fun toDurationTrends(dtos: List<DurationTrendDto>): List<DurationTrend> =
        dtos.map { dto ->
            DurationTrend(
                date = dto.date,
                avgDurationSec = dto.avgDurationSec,
                runCount = dto.runCount,
            )
        }
}
