package com.orchestradashboard.shared.domain.model

data class DashboardUiState(
    val agents: List<Agent> = emptyList(),
    val selectedAgent: Agent? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val filter: Agent.AgentStatus? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val statusFilter: Agent.AgentStatus? = null,
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val timeSeriesData: List<TimeSeriesData> = emptyList(),
) {
    val filteredAgents: List<Agent>
        get() = if (statusFilter == null) agents else agents.filter { it.status == statusFilter }

    val hasNextPage: Boolean get() = currentPage < totalPages - 1
    val hasPreviousPage: Boolean get() = currentPage > 0
}

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    RECONNECTING,
}
