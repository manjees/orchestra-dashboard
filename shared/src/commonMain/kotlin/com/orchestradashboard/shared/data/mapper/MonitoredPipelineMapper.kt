package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineStepDto
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.MonitoredStep
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class MonitoredPipelineMapper {
    fun mapToDomain(dto: OrchestratorPipelineDto): MonitoredPipeline =
        MonitoredPipeline(
            id = dto.id,
            projectName = dto.projectName,
            issueNum = dto.issueNum,
            issueTitle = dto.issueTitle,
            mode = dto.mode,
            status = parseRunStatus(dto.status),
            steps = dto.steps.map { mapStep(it) },
            startedAtMs = parseIsoToEpochMs(dto.startedAt),
            elapsedTotalSec = dto.elapsedTotalSec,
        )

    private fun mapStep(dto: PipelineStepDto): MonitoredStep {
        val status = parseStepStatus(dto.status)
        return MonitoredStep(
            name = dto.name,
            status = status,
            elapsedMs = (dto.elapsedSec * 1000).toLong(),
            startedAtMs =
                if (status == StepStatus.RUNNING) {
                    Clock.System.now().toEpochMilliseconds() - (dto.elapsedSec * 1000).toLong()
                } else {
                    null
                },
            detail = "",
        )
    }

    fun parseStepStatus(status: String): StepStatus =
        when (status.uppercase()) {
            "RUNNING" -> StepStatus.RUNNING
            "PASSED", "SUCCESS" -> StepStatus.PASSED
            "FAILED", "ERROR" -> StepStatus.FAILED
            "SKIPPED" -> StepStatus.SKIPPED
            else -> StepStatus.PENDING
        }

    fun parseRunStatus(status: String): PipelineRunStatus =
        when (status.uppercase()) {
            "RUNNING" -> PipelineRunStatus.RUNNING
            "PASSED", "SUCCESS" -> PipelineRunStatus.PASSED
            "FAILED", "ERROR" -> PipelineRunStatus.FAILED
            "CANCELLED" -> PipelineRunStatus.CANCELLED
            "QUEUED" -> PipelineRunStatus.QUEUED
            else -> PipelineRunStatus.QUEUED
        }

    private fun parseIsoToEpochMs(iso: String): Long? =
        try {
            Instant.parse(iso).toEpochMilliseconds()
        } catch (_: Exception) {
            null
        }
}
