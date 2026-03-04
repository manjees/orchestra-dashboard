package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.repository.AgentRepository

/**
 * Retrieves a single agent by ID.
 *
 * @param agentRepository Data source for agent information
 */
class GetAgentUseCase(
    private val agentRepository: AgentRepository,
) {
    /**
     * Invokes the use case.
     *
     * @param agentId Unique agent identifier
     * @return [Result] containing the agent on success, or an exception on failure
     */
    suspend operator fun invoke(agentId: String): Result<Agent> = agentRepository.getAgent(agentId)
}
