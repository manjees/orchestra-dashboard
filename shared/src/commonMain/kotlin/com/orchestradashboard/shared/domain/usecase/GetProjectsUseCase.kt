package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.repository.ProjectRepository

class GetProjectsUseCase(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(): Result<List<Project>> = repository.getProjects()
}
