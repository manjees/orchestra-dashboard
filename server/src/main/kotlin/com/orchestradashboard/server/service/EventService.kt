package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEventEntity
import com.orchestradashboard.server.model.AgentEventMapper
import com.orchestradashboard.server.model.AgentEventResponse
import com.orchestradashboard.server.model.CreateEventRequest
import com.orchestradashboard.server.repository.AgentEventJpaRepository
import com.orchestradashboard.server.repository.AgentJpaRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EventService(
    private val eventRepository: AgentEventJpaRepository,
    private val agentRepository: AgentJpaRepository,
    private val mapper: AgentEventMapper,
) {
    fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventResponse> {
        val clamped = limit.coerceAtMost(100)
        val entities =
            if (agentId != null) {
                eventRepository.findByAgentIdOrderByTimestampDesc(agentId)
            } else {
                eventRepository.findTop50ByOrderByTimestampDesc()
            }
        return mapper.toResponseList(entities.take(clamped))
    }

    fun createEvent(request: CreateEventRequest): AgentEventResponse {
        if (!agentRepository.existsById(request.agentId)) {
            throw NoSuchElementException("Agent with id '${request.agentId}' not found")
        }
        val entity =
            AgentEventEntity(
                id = UUID.randomUUID().toString(),
                agentId = request.agentId,
                type = request.type,
                payload = request.payload,
                timestamp = System.currentTimeMillis(),
            )
        return mapper.toResponse(eventRepository.save(entity))
    }
}
