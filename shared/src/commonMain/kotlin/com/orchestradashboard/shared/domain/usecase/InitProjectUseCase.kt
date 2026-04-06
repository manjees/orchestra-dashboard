package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.CommandResult
import com.orchestradashboard.shared.domain.model.InitProjectRequest
import com.orchestradashboard.shared.domain.repository.CommandRepository

class InitProjectUseCase(private val repository: CommandRepository) {
    suspend operator fun invoke(request: InitProjectRequest): Result<CommandResult> =
        repository.initProject(request)
}
