package com.orchestradashboard.shared.ui.agentdetail

import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeAgentRepository : AgentRepository {
    val agentsFlow = MutableSharedFlow<List<Agent>>()

    var getAgentResult: Result<Agent> = Result.failure(NoSuchElementException("Agent not found"))
    var shouldFailObserve: Boolean = false
    var observeError: Throwable = RuntimeException("Connection failed")

    override fun observeAgents(): Flow<List<Agent>> {
        if (shouldFailObserve) return flow { throw observeError }
        return agentsFlow
    }

    override fun observeAgent(agentId: String): Flow<Agent> = flow { throw NotImplementedError() }

    override suspend fun getAgent(agentId: String): Result<Agent> = getAgentResult

    override suspend fun getAgentsByStatus(status: Agent.AgentStatus) = Result.failure<List<Agent>>(NotImplementedError())

    override suspend fun registerAgent(agent: Agent) = Result.failure<Agent>(NotImplementedError())

    override suspend fun deregisterAgent(agentId: String) = Result.failure<Unit>(NotImplementedError())

    override fun observeAgents(
        page: Int,
        pageSize: Int,
    ): Flow<PagedResult<Agent>> = flowOf(PagedResult(emptyList(), 0, pageSize, 0, 0))

    override suspend fun invalidateCache() {}
}
