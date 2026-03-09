package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveEventsUseCaseTest {
    private val events =
        listOf(
            AgentEvent("e1", "agent-1", EventType.HEARTBEAT, "{}", 1000L),
            AgentEvent("e2", "agent-1", EventType.STATUS_CHANGE, "{\"status\":\"RUNNING\"}", 2000L),
            AgentEvent("e3", "agent-2", EventType.ERROR, "{\"msg\":\"timeout\"}", 3000L),
        )

    @Test
    fun `invoke returns events filtered by agent ID`() =
        runTest {
            val repository = FakeEventRepository(events)
            val useCase = ObserveEventsUseCase(repository)

            val result = useCase("agent-1").first()

            assertEquals(2, result.size)
            assertTrue(result.all { it.agentId == "agent-1" })
        }

    @Test
    fun `invoke returns empty list for agent with no events`() =
        runTest {
            val repository = FakeEventRepository(events)
            val useCase = ObserveEventsUseCase(repository)

            val result = useCase("agent-999").first()

            assertTrue(result.isEmpty())
        }
}
