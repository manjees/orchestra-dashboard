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
import kotlinx.coroutines.flow.map

class AgentRepositoryImpl(
    private val api: DashboardApi,
    private val agentMapper: AgentMapper,
) : AgentRepository {
    companion object {
        // 5000ms — matches DashboardApiClient default polling interval
        private const val POLLING_INTERVAL_MS = 5000L
    }

    override fun observeAgents(): Flow<List<Agent>> =
        api.observeAgents()
            .map { dtos -> agentMapper.toDomain(dtos) }

    override fun observeAgent(agentId: String): Flow<Agent> =
        flow {
            while (true) {
                emit(agentMapper.toDomain(api.getAgent(agentId)))
                delay(POLLING_INTERVAL_MS)
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
