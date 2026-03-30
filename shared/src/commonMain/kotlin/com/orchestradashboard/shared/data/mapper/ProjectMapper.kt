package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.domain.model.Project

class ProjectMapper {
    fun toDomain(dto: ProjectDto): Project =
        Project(
            name = dto.name,
            path = dto.path,
            ciCommands = dto.ciCommands,
            openIssuesCount = dto.openIssuesCount,
            recentSolves = dto.recentSolves,
        )

    fun toDomain(dtos: List<ProjectDto>): List<Project> = dtos.map(::toDomain)
}
