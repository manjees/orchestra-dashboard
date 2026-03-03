package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.AgentResponse
import com.orchestradashboard.server.model.CreateAgentRequest
import com.orchestradashboard.server.model.UpdateAgentStatusRequest
import com.orchestradashboard.server.model.toResponse
import com.orchestradashboard.server.repository.ServerAgentRepository
import org.springframework.stereotype.Service

/**
 * Business logic for agent lifecycle management.
 *
 * @param agentRepository Persistence layer for agent entities
 */
@Service
class AgentService(
    private val agentRepository: ServerAgentRepository
) {

    /**
     * Retrieves all registered agents.
     *
     * @return List of agent response DTOs
     */
    fun getAllAgents(): List<AgentResponse> {
        return agentRepository.findAll().map { it.toResponse() }
    }

    /**
     * Retrieves a specific agent by its ID.
     *
     * @param agentId Unique agent identifier
     * @return Agent response DTO, or null if not found
     */
    fun getAgent(agentId: String): AgentResponse? {
        return agentRepository.findByAgentId(agentId)?.toResponse()
    }

    /**
     * Registers a new agent with the monitoring system.
     *
     * @param request Registration request with agent details
     * @return The created agent response DTO
     * @throws IllegalArgumentException if an agent with the same ID already exists
     */
    fun registerAgent(request: CreateAgentRequest): AgentResponse {
        require(agentRepository.findByAgentId(request.id) == null) {
            "Agent with id '${request.id}' is already registered"
        }
        val entity = AgentEntity(
            agentId = request.id,
            name = request.name,
            type = request.type,
            status = "OFFLINE",
            lastHeartbeat = System.currentTimeMillis(),
            metadata = request.metadata
        )
        return agentRepository.save(entity).toResponse()
    }

    /**
     * Updates an agent's status and heartbeat timestamp.
     *
     * @param agentId Unique agent identifier
     * @param request Status update payload
     * @return Updated agent response DTO, or null if agent not found
     */
    fun updateAgentStatus(agentId: String, request: UpdateAgentStatusRequest): AgentResponse? {
        val existing = agentRepository.findByAgentId(agentId) ?: return null
        val updated = existing.copy(
            status = request.status,
            lastHeartbeat = request.lastHeartbeat
        )
        return agentRepository.save(updated).toResponse()
    }

    /**
     * Removes an agent from the monitoring system.
     *
     * @param agentId Unique agent identifier
     * @return true if the agent was removed, false if it was not found
     */
    fun deregisterAgent(agentId: String): Boolean {
        val entity = agentRepository.findByAgentId(agentId) ?: return false
        agentRepository.delete(entity)
        return true
    }
}
