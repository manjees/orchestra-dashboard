package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Platform-agnostic ViewModel for the dashboard screen.
 * Uses a plain [CoroutineScope] so it can be shared across Android, iOS, and Desktop.
 *
 * @param observeAgentsUseCase Use case for streaming agent updates
 * @param getAgentUseCase Use case for fetching a single agent
 */
class DashboardViewModel(
    private val observeAgentsUseCase: ObserveAgentsUseCase,
    private val getAgentUseCase: GetAgentUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(DashboardUiState())

    /** Immutable UI state observed by the composable layer */
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Begins real-time observation of the agent fleet.
     * Each call launches a new collection coroutine under the SupervisorJob scope.
     */
    fun startObserving() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            observeAgentsUseCase()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Unknown error",
                            isLoading = false,
                            connectionStatus = ConnectionStatus.DISCONNECTED,
                        )
                    }
                }
                .collect { agents ->
                    _uiState.update {
                        it.copy(
                            agents = agents,
                            isLoading = false,
                            connectionStatus = ConnectionStatus.CONNECTED,
                        )
                    }
                }
        }
    }

    /**
     * Selects an agent for detail view.
     *
     * @param agentId ID of the agent to select; null to deselect
     */
    fun selectAgent(agentId: String?) {
        if (agentId == null) {
            _uiState.update { it.copy(selectedAgent = null) }
            return
        }
        viewModelScope.launch {
            getAgentUseCase(agentId).fold(
                onSuccess = { agent -> _uiState.update { it.copy(selectedAgent = agent) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message ?: "Unknown error") } },
            )
        }
    }

    /** Sets the status filter. Pass null to show all agents. */
    fun setFilter(status: Agent.AgentStatus?) {
        _uiState.update { it.copy(filter = status) }
    }

    /** Clears the current error state */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Must be called when the screen is destroyed to cancel background coroutines */
    fun onCleared() {
        viewModelScope.cancel()
    }
}
