package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.AgentMapper
import com.orchestradashboard.shared.data.network.DashboardApiClient
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Default implementation of [AgentRepository] backed by the remote API.
 *
 * @param apiClient HTTP + WebSocket client for the dashboard server
 * @param agentMapper Maps between network DTOs and domain models
 */
class AgentRepositoryImpl(
    private val apiClient: DashboardApiClient,
    private val agentMapper: AgentMapper,
) : AgentRepository {
    override fun observeAgents(): Flow<List<Agent>> {
        return apiClient.agentUpdates()
            .map { dtos -> agentMapper.toDomain(dtos) }
    }

    override suspend fun getAgent(agentId: String): Result<Agent> {
        return apiClient.fetchAgent(agentId)
            .mapCatching { dto -> agentMapper.toDomain(dto) }
    }

    override suspend fun registerAgent(agent: Agent): Result<Agent> {
        return apiClient.registerAgent(agent)
            .mapCatching { dto -> agentMapper.toDomain(dto) }
    }

    override suspend fun deregisterAgent(agentId: String): Result<Unit> {
        return apiClient.deregisterAgent(agentId)
    }
}
