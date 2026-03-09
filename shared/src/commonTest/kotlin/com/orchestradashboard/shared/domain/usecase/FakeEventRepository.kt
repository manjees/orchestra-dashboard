package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeEventRepository(
    private val events: List<AgentEvent> = emptyList(),
) : EventRepository {
    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> = flowOf(events.filter { it.agentId == agentId })

    override suspend fun getRecentEvents(limit: Int): Result<List<AgentEvent>> =
        Result.success(events.sortedByDescending { it.timestamp }.take(limit))
}
