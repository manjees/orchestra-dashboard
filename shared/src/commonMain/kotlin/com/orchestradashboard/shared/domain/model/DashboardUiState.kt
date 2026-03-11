package com.orchestradashboard.shared.domain.model

/**
 * Represents the complete UI state for the main dashboard screen.
 * All properties are immutable; transitions use [copy].
 */
data class DashboardUiState(
    val agents: List<Agent> = emptyList(),
    val selectedAgent: Agent? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: Agent.AgentStatus? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
) {
    /** Agents filtered by current status filter; all agents when filter is null */
    val filteredAgents: List<Agent>
        get() = if (filter == null) agents else agents.filter { it.status == filter }
}

/** Represents the WebSocket connection lifecycle state */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    RECONNECTING,
}
