package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.AgentMapper
import com.orchestradashboard.server.model.AgentRegistrationRequest
import com.orchestradashboard.server.model.AgentResponse
import com.orchestradashboard.server.repository.AgentJpaRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AgentService(
    private val agentRepository: AgentJpaRepository,
    private val agentMapper: AgentMapper,
) {
    fun getAllAgents(): List<AgentResponse> = agentMapper.toResponseList(agentRepository.findAll())

    fun getAgent(id: String): AgentResponse {
        val entity =
            agentRepository.findById(id)
                .orElseThrow { NoSuchElementException("Agent with id '$id' not found") }
        return agentMapper.toResponse(entity)
    }

    fun getAgentsByStatus(status: String): List<AgentResponse> = agentMapper.toResponseList(agentRepository.findByStatus(status))

    fun registerAgent(request: AgentRegistrationRequest): AgentResponse {
        val id = request.id ?: UUID.randomUUID().toString()
        val entity =
            AgentEntity(
                id = id,
                name = request.name,
                type = request.type,
                status = "OFFLINE",
                lastHeartbeat = System.currentTimeMillis(),
                metadata = agentMapper.serializeMetadata(request.metadata),
            )
        return agentMapper.toResponse(agentRepository.save(entity))
    }

    fun updateHeartbeat(
        id: String,
        status: String,
    ): AgentResponse {
        val existing =
            agentRepository.findById(id)
                .orElseThrow { NoSuchElementException("Agent with id '$id' not found") }
        val updated =
            existing.copy(
                status = status,
                lastHeartbeat = System.currentTimeMillis(),
            )
        return agentMapper.toResponse(agentRepository.save(updated))
    }
}
