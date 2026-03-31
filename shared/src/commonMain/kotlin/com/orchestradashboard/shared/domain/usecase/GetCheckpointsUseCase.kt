package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.repository.CheckpointRepository

class GetCheckpointsUseCase(
    private val repository: CheckpointRepository,
) {
    suspend operator fun invoke(): Result<List<Checkpoint>> = repository.getFailedCheckpoints()
}
