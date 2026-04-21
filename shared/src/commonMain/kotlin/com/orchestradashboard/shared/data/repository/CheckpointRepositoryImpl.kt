package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.CheckpointMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.repository.CheckpointRepository

class CheckpointRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: CheckpointMapper,
) : CheckpointRepository {
    override suspend fun getFailedCheckpoints(): Result<List<Checkpoint>> =
        runCatching {
            api.getCheckpoints()
                .let(mapper::toDomain)
                .filter { it.status == CheckpointStatus.FAILED }
        }

    override suspend fun retryCheckpoint(checkpointId: String): Result<Checkpoint> =
        runCatching {
            mapper.toDomain(api.retryCheckpoint(checkpointId))
        }
}
