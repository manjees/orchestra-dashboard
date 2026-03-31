package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import kotlinx.datetime.Instant

class CheckpointMapper {
    fun toDomain(dto: CheckpointDto): Checkpoint =
        Checkpoint(
            id = dto.id,
            pipelineId = dto.pipelineId,
            createdAt = Instant.parse(dto.createdAt),
            step = dto.step,
            status = parseStatus(dto.status),
        )

    fun toDomain(dtos: List<CheckpointDto>): List<Checkpoint> = dtos.map(::toDomain)

    private fun parseStatus(s: String): CheckpointStatus =
        when (s.lowercase()) {
            "passed" -> CheckpointStatus.PASSED
            "failed" -> CheckpointStatus.FAILED
            "running" -> CheckpointStatus.RUNNING
            "pending" -> CheckpointStatus.PENDING
            else -> CheckpointStatus.UNKNOWN
        }
}
