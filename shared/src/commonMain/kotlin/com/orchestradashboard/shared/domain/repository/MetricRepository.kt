package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.Metric
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing agent performance metrics.
 */
interface MetricRepository {
    /**
     * Observes the latest metrics for a specific agent.
     *
     * @param agentId The agent whose metrics to observe
     * @return [Flow] of metric lists, updated on each metric event
     */
    fun observeMetrics(agentId: String): Flow<List<Metric>>

    /**
     * Retrieves the current metric snapshot for all agents.
     *
     * @return [Result] containing fleet-wide metrics on success
     */
    suspend fun getFleetMetrics(): Result<List<Metric>>
}
