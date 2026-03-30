package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.domain.model.Issue
import kotlinx.datetime.Instant

class IssueMapper {
    fun toDomain(dto: OrchestratorIssueDto): Issue =
        Issue(
            number = dto.number,
            title = dto.title,
            labels = dto.labels,
            state = dto.state,
            createdAt = Instant.parse(dto.createdAt),
        )

    fun toDomain(dtos: List<OrchestratorIssueDto>): List<Issue> = dtos.map(::toDomain)
}
