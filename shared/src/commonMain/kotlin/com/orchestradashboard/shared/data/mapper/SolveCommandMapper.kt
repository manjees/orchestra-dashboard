package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.model.SolveResponse

class SolveCommandMapper {
    fun toDto(request: SolveRequest): SolveCommandRequestDto =
        SolveCommandRequestDto(
            projectName = request.projectName,
            issueNumbers = request.issueNumbers,
            mode = request.mode.toApiString(),
            parallel = request.parallel,
        )

    fun toDomain(dto: SolveCommandResponseDto): SolveResponse =
        SolveResponse(
            pipelineId = dto.pipelineId,
            status = dto.status,
        )

    private fun SolveMode.toApiString(): String =
        when (this) {
            SolveMode.EXPRESS -> "express"
            SolveMode.STANDARD -> "standard"
            SolveMode.FULL -> "full"
            SolveMode.AUTO -> "auto"
        }
}
