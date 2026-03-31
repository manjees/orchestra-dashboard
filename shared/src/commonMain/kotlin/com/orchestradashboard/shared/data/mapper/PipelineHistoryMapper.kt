package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.PipelineRunStatus

class PipelineHistoryMapper {
    fun toDomain(dto: PipelineHistoryDto): PipelineResult =
        PipelineResult(
            id = dto.id,
            projectName = dto.projectName,
            issueNum = dto.issueNum,
            status = parseStatus(dto.status),
            elapsedTotalSec = dto.elapsedTotalSec,
            completedAt = dto.completedAt,
        )

    fun toDomainList(dtos: List<PipelineHistoryDto>): List<PipelineResult> = dtos.map(::toDomain)

    private fun parseStatus(value: String): PipelineRunStatus =
        PipelineRunStatus.entries.find { it.name.equals(value, ignoreCase = true) }
            ?: PipelineRunStatus.FAILED
}
