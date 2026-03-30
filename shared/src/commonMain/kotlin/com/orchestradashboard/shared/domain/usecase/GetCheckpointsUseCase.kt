package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.repository.ProjectRepository

class GetCheckpointsUseCase(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(): Result<List<Checkpoint>> = repository.getCheckpoints()
}
