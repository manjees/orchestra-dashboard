package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for [AgentEntity] persistence.
 */
@Repository
interface ServerAgentRepository : JpaRepository<AgentEntity, Long> {
    /**
     * Finds an agent by its domain-level agent ID (not the DB surrogate key).
     *
     * @param agentId Unique business identifier of the agent
     * @return The entity if found, null otherwise
     */
    fun findByAgentId(agentId: String): AgentEntity?

    /**
     * Finds all agents currently in the specified status.
     *
     * @param status Status string (e.g., "RUNNING", "IDLE")
     * @return List of matching agent entities
     */
    fun findAllByStatus(status: String): List<AgentEntity>

    /**
     * Finds agents whose heartbeat is older than the given timestamp (potentially offline).
     *
     * @param threshold Epoch millis; agents with lastHeartbeat older than this are returned
     * @return List of potentially stale agent entities
     */
    @Query("SELECT a FROM AgentEntity a WHERE a.lastHeartbeat < :threshold AND a.status != 'OFFLINE'")
    fun findStaleAgents(threshold: Long): List<AgentEntity>
}
