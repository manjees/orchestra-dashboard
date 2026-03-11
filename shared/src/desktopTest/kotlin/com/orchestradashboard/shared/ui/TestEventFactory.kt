package com.orchestradashboard.shared.ui

import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType

object TestEventFactory {
    fun create(
        id: String = "evt-1",
        agentId: String = "agent-1",
        type: EventType = EventType.HEARTBEAT,
        payload: String = "{}",
        timestamp: Long = 1000L,
    ): AgentEvent = AgentEvent(id, agentId, type, payload, timestamp)

    fun createList(): List<AgentEvent> =
        listOf(
            create(id = "evt-1", type = EventType.HEARTBEAT, payload = "{}", timestamp = 3000L),
            create(id = "evt-2", type = EventType.STATUS_CHANGE, payload = "{\"status\":\"RUNNING\"}", timestamp = 2000L),
            create(id = "evt-3", type = EventType.ERROR, payload = "{\"message\":\"timeout\"}", timestamp = 1000L),
        )
}
