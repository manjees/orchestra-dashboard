package com.orchestradashboard.shared.domain.model

/**
 * Represents an AI agent being monitored by the dashboard.
 *
 * @param id Unique identifier for the agent
 * @param name Human-readable agent name
 * @param type The role the agent plays in the pipeline
 * @param status Current operational status of the agent
 * @param lastHeartbeat Unix epoch milliseconds of the last heartbeat received
 * @param createdAt Unix epoch milliseconds when the agent was first registered
 * @param metadata Arbitrary key-value pairs for agent-specific data
 */
data class Agent(
    val id: String,
    val name: String,
    val type: AgentType,
    val status: AgentStatus,
    val lastHeartbeat: Long,
    val createdAt: Long = 0L,
    val metadata: Map<String, String> = emptyMap(),
) {
    /** Functional role of the agent within the orchestration pipeline */
    enum class AgentType {
        ORCHESTRATOR,
        WORKER,
        REVIEWER,
        PLANNER,
    }

    /** Current operational state of the agent */
    enum class AgentStatus {
        RUNNING,
        IDLE,
        ERROR,
        OFFLINE,
    }

    /** Formatted display string combining name and type */
    val displayName: String get() = "$name (${type.name.lowercase()})"

    /** True if the agent is in an operational (non-error, non-offline) state */
    val isHealthy: Boolean get() = status == AgentStatus.RUNNING || status == AgentStatus.IDLE
}
