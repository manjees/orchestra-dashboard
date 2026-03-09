package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.PipelineRun
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing and observing pipeline runs.
 * Streaming operations return [Flow]; one-shot operations return [Result].
 */
interface PipelineRepository {
    /**
     * Observes pipeline runs for a specific agent.
     *
     * @param agentId The agent whose pipeline runs to observe
     * @return [Flow] of pipeline run lists, updated on changes
     */
    fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>>

    /**
     * Retrieves a specific pipeline run by its unique identifier.
     *
     * @param runId Unique pipeline run identifier
     * @return [Result] containing the pipeline run on success, or an exception on failure
     */
    suspend fun getPipelineRun(runId: String): Result<PipelineRun>

    /**
     * Observes all currently active (running or queued) pipelines.
     *
     * @return [Flow] of active pipeline run lists, updated on changes
     */
    fun observeActivePipelines(): Flow<List<PipelineRun>>
}
