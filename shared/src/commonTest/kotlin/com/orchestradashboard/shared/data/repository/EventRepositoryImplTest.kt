package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.mapper.AgentEventMapper
import com.orchestradashboard.shared.data.network.FakeDashboardApiClient
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventRepositoryImplTest {
    private val fakeApi = FakeDashboardApiClient()
    private val mapper = AgentEventMapper()
    private val repository = EventRepositoryImpl(fakeApi, mapper, pollingIntervalMs = 50L)

    private val sampleEventDto =
        AgentEventDto(
            id = "evt-1",
            agentId = "agent-1",
            type = "STATUS_CHANGE",
            timestamp = 1700000000L,
        )

    @Test
    fun `getRecentEvents returns Result success with mapped events`() =
        runTest {
            fakeApi.eventsResponse = listOf(sampleEventDto)

            val result = repository.getRecentEvents(50)

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
            assertEquals("evt-1", result.getOrThrow()[0].id)
            assertEquals(EventType.STATUS_CHANGE, result.getOrThrow()[0].type)
        }

    @Test
    fun `getRecentEvents returns Result failure on network error`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("Network error")

            val result = repository.getRecentEvents(50)

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }

    @Test
    fun `observeEvents emits events for given agentId`() =
        runTest {
            fakeApi.eventsResponse =
                listOf(
                    sampleEventDto,
                    AgentEventDto("evt-2", "agent-2", "HEARTBEAT", emptyMap(), 200L),
                )

            val events = repository.observeEvents("agent-1").first()

            assertEquals(1, events.size)
            assertEquals("evt-1", events[0].id)
        }

    @Test
    fun `observeEvents emits periodically`() =
        runTest {
            fakeApi.eventsResponse = listOf(sampleEventDto)

            val emissions = repository.observeEvents("agent-1").take(3).toList()

            assertEquals(3, emissions.size)
            assertTrue(fakeApi.getRecentEventsCallCount >= 3)
        }
}
