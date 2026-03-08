package com.orchestradashboard.shared.domain.model

/**
 * Type of event emitted by an agent.
 */
enum class EventType {
    HEARTBEAT,
    STATUS_CHANGE,
    PIPELINE_STARTED,
    PIPELINE_COMPLETED,
    ERROR,
}

/**
 * Represents a discrete event emitted by an agent during operation.
 *
 * @param id Unique identifier for this event
 * @param agentId The agent that emitted the event
 * @param type Category of the event
 * @param payload Serialized event data
 * @param timestamp Unix epoch milliseconds when the event occurred
 */
data class AgentEvent(
    val id: String,
    val agentId: String,
    val type: EventType,
    val payload: String,
    val timestamp: Long,
)
