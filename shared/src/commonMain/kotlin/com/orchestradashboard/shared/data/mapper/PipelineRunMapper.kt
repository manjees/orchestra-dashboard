package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.PipelineStepDto
import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.PipelineStep
import com.orchestradashboard.shared.domain.model.StepStatus

class PipelineRunMapper {
    fun toDomain(dto: PipelineRunDto): PipelineRun {
        return PipelineRun(
            id = dto.id,
            agentId = dto.agentId,
            pipelineName = dto.pipelineName,
            status = parsePipelineRunStatus(dto.status),
            steps = dto.steps.map(::mapStep),
            startedAt = dto.startedAt,
            finishedAt = dto.finishedAt,
            triggerInfo = dto.triggerInfo,
        )
    }

    fun toDomain(dtos: List<PipelineRunDto>): List<PipelineRun> = dtos.map(::toDomain)

    private fun mapStep(dto: PipelineStepDto): PipelineStep {
        return PipelineStep(
            name = dto.name,
            status = parseStepStatus(dto.status),
            detail = dto.detail,
            elapsedMs = dto.elapsedMs,
        )
    }

    private fun parsePipelineRunStatus(raw: String): PipelineRunStatus {
        return PipelineRunStatus.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: PipelineRunStatus.QUEUED
    }

    private fun parseStepStatus(raw: String): StepStatus {
        return StepStatus.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: StepStatus.PENDING
    }
}
