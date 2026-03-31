package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.repository.CheckpointRepository

class RetryCheckpointUseCase(
    private val repository: CheckpointRepository,
) {
    suspend operator fun invoke(checkpointId: String): Result<Checkpoint> =
        repository.retryCheckpoint(checkpointId)
}
