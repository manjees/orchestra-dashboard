package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.mapper.AgentMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.Agent.AgentStatus
import com.orchestradashboard.shared.domain.repository.AgentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class AgentRepositoryImpl(
    private val api: DashboardApi,
    private val agentMapper: AgentMapper,
    private val pollingIntervalMs: Long = 5_000L,
) : AgentRepository {
    override fun observeAgents(): Flow<List<Agent>> =
        flow {
            while (true) {
                val agents = api.getAgents()
                emit(agentMapper.toDomain(agents))
                delay(pollingIntervalMs)
            }
        }

    override suspend fun getAgent(agentId: String): Result<Agent> =
        runCatching {
            agentMapper.toDomain(api.getAgent(agentId))
        }

    override suspend fun getAgentsByStatus(status: AgentStatus): Result<List<Agent>> =
        runCatching {
            observeAgents().first().filter { it.status == status }
        }

    override suspend fun registerAgent(agent: Agent): Result<Agent> =
        runCatching {
            val dto =
                AgentDto(
                    id = agent.id,
                    name = agent.name,
                    type = agent.type.name,
                    status = agent.status.name,
                    lastHeartbeat = agent.lastHeartbeat,
                    metadata = agent.metadata,
                )
            agentMapper.toDomain(api.registerAgent(dto))
        }

    override suspend fun deregisterAgent(agentId: String): Result<Unit> =
        runCatching {
            api.deregisterAgent(agentId)
        }
}
