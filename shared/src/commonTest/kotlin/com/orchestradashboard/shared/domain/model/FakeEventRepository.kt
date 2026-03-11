package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakeEventRepository : EventRepository {
    val eventsFlow = MutableSharedFlow<List<AgentEvent>>()

    var shouldFailObserve: Boolean = false
    var observeError: Throwable = RuntimeException("Event connection failed")

    override fun observeEvents(agentId: String): Flow<List<AgentEvent>> {
        if (shouldFailObserve) return flow { throw observeError }
        return eventsFlow
    }

    override suspend fun getRecentEvents(limit: Int): Result<List<AgentEvent>> = Result.failure(NotImplementedError())
}
