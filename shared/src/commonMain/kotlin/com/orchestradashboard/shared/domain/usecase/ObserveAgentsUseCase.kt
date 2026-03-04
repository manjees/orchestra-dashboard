package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the real-time list of all monitored agents.
 *
 * @param agentRepository Data source for agent information
 */
class ObserveAgentsUseCase(
    private val agentRepository: AgentRepository,
) {
    /**
     * Invokes the use case.
     *
     * @return [Flow] emitting updated agent lists on each state change
     */
    operator fun invoke(): Flow<List<Agent>> = agentRepository.observeAgents()
}
