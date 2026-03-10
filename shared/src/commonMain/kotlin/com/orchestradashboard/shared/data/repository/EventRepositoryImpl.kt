package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.AgentEventMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.repository.EventRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EventRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: AgentEventMapper,
    private val pollingIntervalMs: Long = 5_000L,
) : EventRepository {
    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> =
        flow {
            while (true) {
                val events = api.getRecentEvents(agentId = agentId)
                emit(mapper.toDomain(events))
                delay(pollingIntervalMs)
            }
        }

    override suspend fun getRecentEvents(limit: Int): Result<List<AgentEvent>> =
        runCatching {
            mapper.toDomain(api.getRecentEvents(limit = limit))
        }
}
