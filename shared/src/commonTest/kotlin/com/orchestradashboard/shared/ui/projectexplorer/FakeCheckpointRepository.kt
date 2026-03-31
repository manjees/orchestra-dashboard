package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.repository.CheckpointRepository

class FakeCheckpointRepository : CheckpointRepository {
    var getFailedCheckpointsResult: Result<List<Checkpoint>> = Result.success(emptyList())
    var retryCheckpointResult: Result<Checkpoint> = Result.failure(NotImplementedError())

    var getFailedCheckpointsCallCount = 0
        private set
    var retryCheckpointCallCount = 0
        private set
    var lastRetriedCheckpointId: String? = null
        private set

    override suspend fun getFailedCheckpoints(): Result<List<Checkpoint>> {
        getFailedCheckpointsCallCount++
        return getFailedCheckpointsResult
    }

    override suspend fun retryCheckpoint(checkpointId: String): Result<Checkpoint> {
        retryCheckpointCallCount++
        lastRetriedCheckpointId = checkpointId
        return retryCheckpointResult
    }
}
