package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.repository.ProjectRepository

class GetProjectIssuesUseCase(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(
        name: String,
        page: Int = 0,
        pageSize: Int = 20,
    ): Result<List<Issue>> = repository.getProjectIssues(name, page, pageSize)
}
