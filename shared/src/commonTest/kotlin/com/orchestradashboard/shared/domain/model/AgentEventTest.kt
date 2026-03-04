package com.orchestradashboard.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AgentEventTest {

    @Test
    fun `should construct heartbeat event`() {
        val event = AgentEvent(
            id = "evt-1",
            agentId = "agent-1",
            type = EventType.HEARTBEAT,
            payload = "{}",
            timestamp = 1000L
        )
        assertEquals("evt-1", event.id)
        assertEquals("agent-1", event.agentId)
        assertEquals(EventType.HEARTBEAT, event.type)
        assertEquals("{}", event.payload)
        assertEquals(1000L, event.timestamp)
    }

    @Test
    fun `should construct status change event`() {
        val event = AgentEvent(
            id = "evt-2",
            agentId = "agent-2",
            type = EventType.STATUS_CHANGE,
            payload = """{"from":"IDLE","to":"RUNNING"}""",
            timestamp = 2000L
        )
        assertEquals(EventType.STATUS_CHANGE, event.type)
    }

    @Test
    fun `should construct pipeline started event`() {
        val event = AgentEvent(
            id = "evt-3",
            agentId = "agent-3",
            type = EventType.PIPELINE_STARTED,
            payload = """{"pipelineId":"run-1"}""",
            timestamp = 3000L
        )
        assertEquals(EventType.PIPELINE_STARTED, event.type)
    }

    @Test
    fun `should construct pipeline completed event`() {
        val event = AgentEvent(
            id = "evt-4",
            agentId = "agent-4",
            type = EventType.PIPELINE_COMPLETED,
            payload = """{"pipelineId":"run-1","status":"PASSED"}""",
            timestamp = 4000L
        )
        assertEquals(EventType.PIPELINE_COMPLETED, event.type)
    }

    @Test
    fun `should construct error event`() {
        val event = AgentEvent(
            id = "evt-5",
            agentId = "agent-5",
            type = EventType.ERROR,
            payload = "OutOfMemoryError",
            timestamp = 5000L
        )
        assertEquals(EventType.ERROR, event.type)
    }

    @Test
    fun `should support all EventType values`() {
        val types = EventType.entries
        assertEquals(5, types.size)
        assertEquals(
            listOf(
                EventType.HEARTBEAT,
                EventType.STATUS_CHANGE,
                EventType.PIPELINE_STARTED,
                EventType.PIPELINE_COMPLETED,
                EventType.ERROR
            ),
            types
        )
    }

    @Test
    fun `should support equality for identical events`() {
        val event1 = AgentEvent(
            id = "evt-1",
            agentId = "agent-1",
            type = EventType.HEARTBEAT,
            payload = "{}",
            timestamp = 1000L
        )
        val event2 = AgentEvent(
            id = "evt-1",
            agentId = "agent-1",
            type = EventType.HEARTBEAT,
            payload = "{}",
            timestamp = 1000L
        )
        assertEquals(event1, event2)
    }
}
