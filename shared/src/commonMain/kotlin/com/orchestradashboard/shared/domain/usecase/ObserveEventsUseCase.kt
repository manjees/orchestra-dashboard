package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes events for a specific agent in real-time.
 *
 * @param eventRepository Data source for event information
 */
class ObserveEventsUseCase(
    private val eventRepository: EventRepository,
) {
    /**
     * Invokes the use case.
     *
     * @param agentId The agent whose events to observe
     * @return [Flow] emitting updated event lists for the specified agent
     */
    operator fun invoke(agentId: String): Flow<List<AgentEvent>> = eventRepository.observeEvents(agentId)
}
