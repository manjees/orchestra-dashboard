package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryPageDto
import com.orchestradashboard.shared.data.dto.orchestrator.StepHistoryDto
import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.model.HistoryStep
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus

class HistoryDetailMapper {
    fun toDomain(dto: PipelineHistoryDetailDto): HistoryDetail =
        HistoryDetail(
            id = dto.id,
            projectName = dto.projectName,
            issueNum = dto.issueNum,
            issueTitle = dto.issueTitle,
            mode = dto.mode,
            status = parseStatus(dto.status),
            startedAt = dto.startedAt,
            completedAt = dto.completedAt,
            elapsedTotalSec = dto.elapsedTotalSec,
            prUrl = dto.prUrl,
            steps = dto.steps.map { toStep(it) },
        )

    fun toPagedDomain(pageDto: PipelineHistoryPageDto): PagedResult<PipelineResult> =
        PagedResult(
            agents = pageDto.content.map { toPipelineResult(it) },
            page = pageDto.number,
            pageSize = pageDto.size,
            totalElements = pageDto.totalElements,
            totalPages = pageDto.totalPages,
        )

    private fun toStep(dto: StepHistoryDto): HistoryStep =
        HistoryStep(
            stepName = dto.stepName,
            status = parseStepStatus(dto.status),
            elapsedSec = dto.elapsedSec,
            failDetail = dto.failDetail,
        )

    private fun toPipelineResult(dto: PipelineHistoryDetailDto): PipelineResult =
        PipelineResult(
            id = dto.id,
            projectName = dto.projectName,
            issueNum = dto.issueNum,
            status = parseStatus(dto.status),
            elapsedTotalSec = dto.elapsedTotalSec,
            completedAt = dto.completedAt?.toString(),
        )

    private fun parseStatus(value: String): PipelineRunStatus =
        PipelineRunStatus.entries.find { it.name.equals(value, ignoreCase = true) }
            ?: PipelineRunStatus.FAILED

    private fun parseStepStatus(value: String): StepStatus =
        StepStatus.entries.find { it.name.equals(value, ignoreCase = true) }
            ?: StepStatus.FAILED
}
