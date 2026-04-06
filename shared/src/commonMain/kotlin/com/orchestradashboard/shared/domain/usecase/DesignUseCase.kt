package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.DesignResult
import com.orchestradashboard.shared.domain.repository.CommandRepository

class DesignUseCase(private val repository: CommandRepository) {
    suspend operator fun invoke(projectName: String, figmaUrl: String): Result<DesignResult> =
        repository.design(projectName, figmaUrl)
}
