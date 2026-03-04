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
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
)

/** Represents the WebSocket connection lifecycle state */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    RECONNECTING,
}
