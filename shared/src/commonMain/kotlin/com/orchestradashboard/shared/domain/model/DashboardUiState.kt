package com.orchestradashboard.shared.domain.model

data class DashboardUiState(
    val agents: List<Agent> = emptyList(),
    val selectedAgent: Agent? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: Agent.AgentStatus? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val statusFilter: Agent.AgentStatus? = null,
) {
    val filteredAgents: List<Agent>
        get() = if (statusFilter == null) agents else agents.filter { it.status == statusFilter }
}

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    RECONNECTING,
}
