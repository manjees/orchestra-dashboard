package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.domain.model.ActivePipeline

class ActivePipelineMapper {
    fun toDomain(dto: OrchestratorPipelineDto): ActivePipeline =
        ActivePipeline(
            id = dto.id,
            projectName = dto.projectName,
            issueNum = dto.issueNum,
            issueTitle = dto.issueTitle,
            currentStep = dto.currentStep ?: "",
            elapsedTotalSec = dto.elapsedTotalSec,
            status = dto.status,
        )

    fun toDomainList(dtos: List<OrchestratorPipelineDto>): List<ActivePipeline> = dtos.map(::toDomain)
}
