package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.AgentEventMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: AgentEventMapper,
) : EventRepository {
    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> =
        api.observeEvents(agentId)
            .map { dtos -> mapper.toDomain(dtos) }

    override suspend fun getRecentEvents(limit: Int): Result<List<AgentEvent>> =
        runCatching {
            mapper.toDomain(api.getRecentEvents(limit = limit))
        }
}
