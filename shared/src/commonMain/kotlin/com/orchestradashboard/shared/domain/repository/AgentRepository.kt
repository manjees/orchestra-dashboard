package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.Agent
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing and observing AI agents.
 * Streaming operations return [Flow]; one-shot operations return [Result].
 */
interface AgentRepository {
    /**
     * Observes all registered agents in real-time.
     *
     * @return [Flow] of agent lists, updated whenever agent state changes
     */
    fun observeAgents(): Flow<List<Agent>>

    /**
     * Retrieves a specific agent by its unique identifier.
     *
     * @param agentId Unique agent identifier
     * @return [Result] containing the agent on success, or an exception on failure
     */
    suspend fun getAgent(agentId: String): Result<Agent>

    /**
     * Registers a new agent with the monitoring system.
     *
     * @param agent The agent to register
     * @return [Result] containing the registered agent (with server-assigned fields) on success
     */
    suspend fun registerAgent(agent: Agent): Result<Agent>

    /**
     * Deregisters an agent from monitoring.
     *
     * @param agentId Unique identifier of the agent to deregister
     * @return [Result] containing Unit on success, or an exception on failure
     */
    suspend fun deregisterAgent(agentId: String): Result<Unit>
}
