package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.mapper.AgentEventMapper
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventRepositoryImplTest {
    private val mapper = AgentEventMapper()

    private fun createFakeClient() = FakeDashboardApiClient(pollingIntervalMs = 5000L)

    private val sampleEventDto =
        AgentEventDto(
            id = "evt-1",
            agentId = "agent-1",
            type = "STATUS_CHANGE",
            payload = JsonObject(mapOf("old" to JsonPrimitive("IDLE"), "new" to JsonPrimitive("RUNNING"))),
            timestamp = 1700000000L,
        )

    private val otherAgentEvent =
        AgentEventDto(
            id = "evt-2",
            agentId = "agent-2",
            type = "HEARTBEAT",
            timestamp = 2000L,
        )

    @Test
    fun `getRecentEvents returns mapped events`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.events = listOf(sampleEventDto, otherAgentEvent)
            val repo = EventRepositoryImpl(fakeClient, mapper)

            val result = repo.getRecentEvents(50)

            assertTrue(result.isSuccess)
            val events = result.getOrThrow()
            assertEquals(2, events.size)
            assertEquals("evt-1", events[0].id)
            assertEquals(EventType.STATUS_CHANGE, events[0].type)
        }

    @Test
    fun `getRecentEvents returns failure on error`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.shouldFail = true
            val repo = EventRepositoryImpl(fakeClient, mapper)

            val result = repo.getRecentEvents(50)

            assertTrue(result.isFailure)
        }

    @Test
    fun `observeEvents emits for given agentId`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.events = listOf(sampleEventDto, otherAgentEvent)
            val repo = EventRepositoryImpl(fakeClient, mapper)

            val result = repo.observeEvents("agent-1").first()

            assertEquals(1, result.size)
            assertEquals("agent-1", result[0].agentId)
            assertEquals(EventType.STATUS_CHANGE, result[0].type)
        }
}
