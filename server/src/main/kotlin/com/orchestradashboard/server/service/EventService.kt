package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentEventEntity
import com.orchestradashboard.server.model.AgentEventMapper
import com.orchestradashboard.server.model.AgentEventResponse
import com.orchestradashboard.server.model.CreateEventRequest
import com.orchestradashboard.server.repository.AgentEventJpaRepository
import com.orchestradashboard.server.repository.AgentJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EventService(
    private val eventRepository: AgentEventJpaRepository,
    private val agentRepository: AgentJpaRepository,
    private val eventMapper: AgentEventMapper,
) {
    companion object {
        const val DEFAULT_LIMIT = 20
        const val MAX_LIMIT = 100
    }

    fun getRecentEvents(limit: Int? = null): List<AgentEventResponse> {
        val effectiveLimit = (limit ?: DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)
        val pageable = PageRequest.of(0, effectiveLimit)
        return eventMapper.toResponseList(eventRepository.findAllByOrderByTimestampDesc(pageable))
    }

    fun getEventsByAgentId(
        agentId: String,
        limit: Int? = null,
    ): List<AgentEventResponse> {
        val effectiveLimit = (limit ?: DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)
        val pageable = PageRequest.of(0, effectiveLimit)
        return eventMapper.toResponseList(eventRepository.findByAgentIdOrderByTimestampDesc(agentId, pageable))
    }

    fun createEvent(request: CreateEventRequest): AgentEventResponse {
        agentRepository.findById(request.agentId)
            .orElseThrow { NoSuchElementException("Agent with id '${request.agentId}' not found") }

        val entity =
            AgentEventEntity(
                id = UUID.randomUUID().toString(),
                agentId = request.agentId,
                type = request.type,
                payload = eventMapper.serializePayload(request.payload),
                timestamp = System.currentTimeMillis(),
            )
        return eventMapper.toResponse(eventRepository.save(entity))
    }
}
