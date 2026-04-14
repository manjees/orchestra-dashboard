package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.DiscussResult
import com.orchestradashboard.shared.domain.repository.CommandRepository

class DiscussUseCase(private val repository: CommandRepository) {
    suspend operator fun invoke(
        projectName: String,
        question: String,
    ): Result<DiscussResult> = repository.discuss(projectName, question)
}
