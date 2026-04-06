package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PlanIssuesResult
import com.orchestradashboard.shared.domain.repository.CommandRepository

class PlanIssuesUseCase(private val repository: CommandRepository) {
    suspend operator fun invoke(projectName: String): Result<PlanIssuesResult> =
        repository.planIssues(projectName)
}
