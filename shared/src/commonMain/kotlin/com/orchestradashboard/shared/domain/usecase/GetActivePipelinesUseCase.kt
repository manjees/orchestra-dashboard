package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.ActivePipeline
import com.orchestradashboard.shared.domain.repository.SystemRepository

class GetActivePipelinesUseCase(private val repository: SystemRepository) {
    suspend operator fun invoke(): Result<List<ActivePipeline>> = repository.getActivePipelines()
}
