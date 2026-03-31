package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.repository.SystemRepository

class GetPipelineHistoryUseCase(private val repository: SystemRepository) {
    suspend operator fun invoke(limit: Int = 10): Result<List<PipelineResult>> = repository.getPipelineHistory().map { it.take(limit) }
}
