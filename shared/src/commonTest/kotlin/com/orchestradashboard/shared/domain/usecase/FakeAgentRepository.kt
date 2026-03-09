package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAgentRepository(
    private val agents: List<Agent> = emptyList(),
) : AgentRepository {
    override fun observeAgents(): Flow<List<Agent>> = flowOf(agents)

    override suspend fun getAgent(agentId: String): Result<Agent> =
        agents.find { it.id == agentId }
            ?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Agent $agentId not found"))

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus): Result<List<Agent>> =
        Result.success(agents.filter { it.status == status })

    override suspend fun registerAgent(agent: Agent): Result<Agent> = Result.success(agent)

    override suspend fun deregisterAgent(agentId: String): Result<Unit> = Result.success(Unit)
}
