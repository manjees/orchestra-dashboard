package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.AgentEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing and observing agent events.
 * Streaming operations return [Flow]; one-shot operations return [Result].
 */
interface EventRepository {
    /**
     * Observes events for a specific agent in real-time.
     *
     * @param agentId The agent whose events to observe
     * @return [Flow] of event lists, updated on changes
     */
    fun observeEvents(agentId: String): Flow<List<AgentEvent>>

    /**
     * Retrieves the most recent events across all agents.
     *
     * @param limit Maximum number of events to return
     * @return [Result] containing the events on success, or an exception on failure
     */
    suspend fun getRecentEvents(limit: Int): Result<List<AgentEvent>>
}
