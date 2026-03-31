package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.Checkpoint

interface CheckpointRepository {
    suspend fun getFailedCheckpoints(): Result<List<Checkpoint>>

    suspend fun retryCheckpoint(checkpointId: String): Result<Checkpoint>
}
