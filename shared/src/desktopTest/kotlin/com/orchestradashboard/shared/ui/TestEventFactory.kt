package com.orchestradashboard.shared.ui

import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.EventType
import kotlinx.datetime.Clock

object TestEventFactory {
    fun create(
        id: String = "event-1",
        agentId: String = "agent-1",
        type: EventType = EventType.HEARTBEAT,
        payload: String = """{"status": "ok"}""",
        timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    ): AgentEvent =
        AgentEvent(
            id = id,
            agentId = agentId,
            type = type,
            payload = payload,
            timestamp = timestamp,
        )

    fun createList(): List<AgentEvent> =
        listOf(
            create(
                id = "evt-1",
                type = EventType.STATUS_CHANGE,
                payload = """{"from": "IDLE", "to": "RUNNING"}""",
                timestamp = Clock.System.now().toEpochMilliseconds() - 30_000L,
            ),
            create(
                id = "evt-2",
                type = EventType.PIPELINE_STARTED,
                payload = """{"pipeline": "deploy"}""",
                timestamp = Clock.System.now().toEpochMilliseconds() - 20_000L,
            ),
            create(
                id = "evt-3",
                type = EventType.ERROR,
                payload = """{"message": "connection timeout"}""",
                timestamp = Clock.System.now().toEpochMilliseconds() - 10_000L,
            ),
        )
}
